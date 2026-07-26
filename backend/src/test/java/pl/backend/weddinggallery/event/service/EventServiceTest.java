package pl.backend.weddinggallery.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.dto.EventWriteRequest;
import pl.backend.weddinggallery.event.model.*;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
	@Mock
	private EventRepository eventRepository;
	@Mock
	private EventMembershipRepository membershipRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private AuditService auditService;

	private EventService service;
	private User owner;

	@BeforeEach
	void setUp() {
		service = new EventService(eventRepository, membershipRepository, userRepository, auditService);
		owner = User.builder().id("owner-id").email("owner@example.com").build();
		when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
	}

	@Test
	void shouldAlwaysTakeOwnerFromAuthenticatedPrincipal() {
		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
			Event event = invocation.getArgument(0);
			event.setId("event-id");
			event.setCreatedAt(LocalDateTime.now());
			event.setUpdatedAt(LocalDateTime.now());
			return event;
		});

		var response = service.create(new EventWriteRequest("  Wedding  ", EventType.WEDDING, null, null,
				PrivacyMode.PRIVATE), "owner@example.com");

		assertThat(response.name()).isEqualTo("Wedding");
		verify(eventRepository).save(argThat(event -> event.getOwner() == owner));
		verify(auditService).logRequiredEvent(eq("owner@example.com"), any(), eq("event-id"), isNull(), anyString());
	}

	@Test
	void shouldMaskInaccessibleEventAsNotFound() {
		when(eventRepository.findAccessibleById("foreign-event", "owner-id")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get("foreign-event", "owner@example.com"))
				.isInstanceOf(AppException.class).hasMessage("Event was not found.");
	}

	@Test
	void shouldPropagateRequiredAuditFailure() {
		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
			Event event = invocation.getArgument(0);
			event.setId("event-id");
			event.setCreatedAt(LocalDateTime.now());
			event.setUpdatedAt(LocalDateTime.now());
			return event;
		});
		doThrow(new IllegalStateException("audit unavailable")).when(auditService)
				.logRequiredEvent(anyString(), any(), anyString(), isNull(), anyString());

		assertThatThrownBy(() -> service.create(new EventWriteRequest("Wedding", EventType.WEDDING, null, null,
				PrivacyMode.PRIVATE), "owner@example.com")).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void shouldNormalizeUserLookupAndDescriptionAndReturnUpdatedResponse() {
		Event event = event(EventStatus.DRAFT);
		when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
		when(eventRepository.findAccessibleById(event.getId(), owner.getId())).thenReturn(Optional.of(event));

		var response = service.update(event.getId(), new EventWriteRequest("  Updated  ", EventType.WEDDING,
				LocalDate.of(2030, 1, 2), "   ", PrivacyMode.PRIVATE), " OWNER@EXAMPLE.COM ");

		assertThat(response.name()).isEqualTo("Updated");
		assertThat(response.description()).isNull();
		assertThat(event.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE);
		verify(auditService).logRequiredEvent(eq(owner.getEmail()), any(), eq(event.getId()), isNull(), anyString());
	}

	@Test
	void shouldRejectUpdateOfArchivedEvent() {
		Event event = event(EventStatus.ARCHIVED);
		when(eventRepository.findAccessibleById(event.getId(), owner.getId())).thenReturn(Optional.of(event));

		assertCode(() -> service.update(event.getId(), request(), owner.getEmail()), EventErrorCode.EVENT_ARCHIVED);
		verifyNoInteractions(auditService);
	}

	@Test
	void shouldArchiveOnlyOwnedEventAndMakeRepeatedArchiveIdempotent() {
		Event event = event(EventStatus.DRAFT);
		when(eventRepository.findAccessibleById(event.getId(), owner.getId())).thenReturn(Optional.of(event));

		assertThat(service.archive(event.getId(), owner.getEmail()).status()).isEqualTo(EventStatus.ARCHIVED);
		service.archive(event.getId(), owner.getEmail());

		verify(auditService, times(1)).logRequiredEvent(eq(owner.getEmail()), any(), eq(event.getId()), isNull(),
				contains("ARCHIVED"));
	}

	@Test
	void shouldRejectArchiveAndDeleteForNonOwnerAndMaskMissingEvent() {
		User manager = User.builder().id("manager-id").email("manager@example.com").build();
		Event event = event(EventStatus.DRAFT);
		when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
		when(eventRepository.findAccessibleById(event.getId(), manager.getId())).thenReturn(Optional.of(event));

		assertCode(() -> service.archive(event.getId(), manager.getEmail()), EventErrorCode.EVENT_OWNER_REQUIRED);
		assertCode(() -> service.delete(event.getId(), manager.getEmail()), EventErrorCode.EVENT_OWNER_REQUIRED);

		when(eventRepository.findAccessibleById("missing", owner.getId())).thenReturn(Optional.empty());
		assertCode(() -> service.delete("missing", owner.getEmail()), EventErrorCode.EVENT_NOT_FOUND);
	}

	@Test
	void shouldListOwnerAndManagerWithRolesAndRejectUnknownRole() {
		User manager = User.builder().id("manager-id").email("manager@example.com").build();
		Event event = event(EventStatus.DRAFT);
		when(membershipRepository.existsByEventIdAndUserIdAndRemovedAtIsNull(event.getId(), manager.getId()))
				.thenReturn(true);

		assertThat(service.roleFor(event, owner)).isEqualTo(pl.backend.weddinggallery.membership.model.EventRole.OWNER);
		assertThat(service.roleFor(event, manager))
				.isEqualTo(pl.backend.weddinggallery.membership.model.EventRole.MANAGER);
		assertCode(() -> service.roleFor(event, User.builder().id("other-id").build()), EventErrorCode.EVENT_NOT_FOUND);
		when(eventRepository.findAccessibleByUserId(owner.getId())).thenReturn(List.of(event));
		assertThat(service.list(owner.getEmail())).singleElement().satisfies(item -> assertThat(item.currentUserRole())
				.isEqualTo(pl.backend.weddinggallery.membership.model.EventRole.OWNER));
	}

	private Event event(EventStatus status) {
		return Event.builder().id("event-id").name("Wedding").type(EventType.WEDDING).status(status).owner(owner)
				.privacyMode(PrivacyMode.PRIVATE).createdAt(LocalDateTime.now().minusDays(1))
				.updatedAt(LocalDateTime.now()).build();
	}

	private EventWriteRequest request() {
		return new EventWriteRequest("Updated", EventType.WEDDING, null, "description", PrivacyMode.PRIVATE);
	}

	private void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, EventErrorCode code) {
		assertThatThrownBy(call).isInstanceOfSatisfying(AppException.class,
				error -> assertThat(error.getErrorCode()).isEqualTo(code));
	}
}
