package pl.backend.weddinggallery.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
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
}
