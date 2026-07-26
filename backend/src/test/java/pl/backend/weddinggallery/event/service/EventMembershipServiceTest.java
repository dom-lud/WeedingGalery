package pl.backend.weddinggallery.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.dto.EventResponse;
import pl.backend.weddinggallery.event.model.*;
import pl.backend.weddinggallery.membership.dto.*;
import pl.backend.weddinggallery.membership.exception.MembershipErrorCode;
import pl.backend.weddinggallery.membership.model.*;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.membership.service.EventMembershipService;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EventMembershipServiceTest {
	@Mock
	private EventMembershipRepository memberships;
	@Mock
	private UserRepository users;
	@Mock
	private EventService events;
	@Mock
	private AuditService audit;
	private EventMembershipService service;
	private User owner;
	private User manager;
	private Event event;

	@BeforeEach
	void setUp() {
		service = new EventMembershipService(memberships, users, events, audit);
		owner = User.builder().id("owner-id").email("owner@example.com").build();
		manager = User.builder().id("manager-id").email("manager@example.com").build();
		event = Event.builder().id("event-id").name("Wedding").type(EventType.WEDDING).status(EventStatus.DRAFT)
				.owner(owner).privacyMode(PrivacyMode.PRIVATE).createdAt(LocalDateTime.now().minusDays(1))
				.updatedAt(LocalDateTime.now()).build();
		when(events.requireUser(owner.getEmail())).thenReturn(owner);
		lenient().when(events.requireOwnedEvent(event.getId(), owner)).thenReturn(event);
	}

	@Test
	void shouldListOwnerAndActiveManagersOnly() {
		EventMembership active = membership("membership-id", manager, null);
		when(events.requireAccessibleEvent(event.getId(), owner)).thenReturn(event);
		when(memberships.findByEventIdAndRemovedAtIsNullOrderByJoinedAtAsc(event.getId())).thenReturn(List.of(active));

		var result = service.list(event.getId(), owner.getEmail());

		assertThat(result).extracting(item -> item.role()).containsExactly(EventRole.OWNER, EventRole.MANAGER);
	}

	@Test
	void shouldAddManagerWithNormalizedEmailAndAudit() {
		when(users.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
		when(memberships.findByEventIdAndUserId(event.getId(), manager.getId())).thenReturn(Optional.empty());
		when(memberships.save(any(EventMembership.class))).thenAnswer(invocation -> {
			EventMembership saved = invocation.getArgument(0);
			saved.setId("membership-id");
			saved.setJoinedAt(LocalDateTime.now());
			return saved;
		});

		var response = service.add(event.getId(), new AddEventManagerRequest(" Manager@Example.COM ", EventRole.MANAGER),
				owner.getEmail());

		assertThat(response.role()).isEqualTo(EventRole.MANAGER);
		verify(memberships).save(argThat(item -> item.getEvent() == event && item.getUser() == manager));
		verify(audit).logRequiredEvent(eq(owner.getEmail()), any(), eq(event.getId()), eq(manager.getId()), anyString());
	}

	@Test
	void shouldRejectInvalidRoleOwnerTargetUnknownUserAndAlreadyActiveMembership() {
		assertCode(() -> service.add(event.getId(), new AddEventManagerRequest("manager@example.com", EventRole.OWNER),
				owner.getEmail()), MembershipErrorCode.INVALID_MEMBERSHIP_TARGET);
		when(users.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
		assertCode(() -> service.add(event.getId(), new AddEventManagerRequest("owner@example.com", EventRole.MANAGER),
				owner.getEmail()), MembershipErrorCode.INVALID_MEMBERSHIP_TARGET);
		when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
		assertCode(() -> service.add(event.getId(),
				new AddEventManagerRequest("missing@example.com", EventRole.MANAGER), owner.getEmail()),
				MembershipErrorCode.USER_NOT_FOUND);
		when(users.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
		when(memberships.findByEventIdAndUserId(event.getId(), manager.getId()))
				.thenReturn(Optional.of(membership("id", manager, null)));
		assertCode(() -> service.add(event.getId(),
				new AddEventManagerRequest("manager@example.com", EventRole.MANAGER), owner.getEmail()),
				MembershipErrorCode.MEMBERSHIP_ALREADY_ACTIVE);
	}

	@Test
	void shouldReactivateRemovedMembership() {
		EventMembership removed = membership("membership-id", manager, LocalDateTime.now().minusDays(1));
		when(users.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
		when(memberships.findByEventIdAndUserId(event.getId(), manager.getId())).thenReturn(Optional.of(removed));

		var response = service.add(event.getId(), new AddEventManagerRequest(manager.getEmail(), EventRole.MANAGER),
				owner.getEmail());

		assertThat(removed.isActive()).isTrue();
		assertThat(response.id()).isEqualTo("membership-id");
		verify(memberships).save(removed);
	}

	@Test
	void shouldRemoveMembershipAndRejectMissingOrForeignMembership() {
		EventMembership target = membership("membership-id", manager, null);
		when(memberships.findByIdAndEventIdAndRemovedAtIsNull(target.getId(), event.getId()))
				.thenReturn(Optional.of(target));

		service.remove(event.getId(), target.getId(), owner.getEmail());
		assertThat(target.isActive()).isFalse();
		verify(audit).logRequiredEvent(eq(owner.getEmail()), any(), eq(event.getId()), eq(manager.getId()),
				anyString());
		when(memberships.findByIdAndEventIdAndRemovedAtIsNull("missing", event.getId())).thenReturn(Optional.empty());
		assertCode(() -> service.remove(event.getId(), "missing", owner.getEmail()),
				MembershipErrorCode.MEMBERSHIP_NOT_FOUND);
	}

	@Test
	void shouldTransferOwnershipAndCreateManagerMembershipForOldOwner() {
		EventMembership target = membership("target-id", manager, null);
		when(memberships.findByIdAndEventIdAndRemovedAtIsNull(target.getId(), event.getId()))
				.thenReturn(Optional.of(target));
		when(memberships.findByEventIdAndUserId(event.getId(), owner.getId())).thenReturn(Optional.empty());
		when(events.toResponse(event, EventRole.MANAGER)).thenReturn(mock(EventResponse.class));

		service.transferOwnership(event.getId(), new OwnershipTransferRequest(target.getId()), owner.getEmail());

		assertThat(event.getOwner()).isSameAs(manager);
		assertThat(target.isActive()).isFalse();
		verify(memberships).save(argThat(item -> item.getUser() == owner && item.getRole() == EventRole.MANAGER));
		verify(audit).logRequiredEvent(eq(owner.getEmail()), any(), eq(event.getId()), eq(manager.getId()),
				contains("OWNER"));
	}

	@Test
	void shouldRejectTransferForMissingMembershipAndNonOwner() {
		when(memberships.findByIdAndEventIdAndRemovedAtIsNull("missing", event.getId())).thenReturn(Optional.empty());
		assertCode(() -> service.transferOwnership(event.getId(), new OwnershipTransferRequest("missing"), owner.getEmail()),
				MembershipErrorCode.MEMBERSHIP_NOT_FOUND);
		User nonOwner = User.builder().id("non-owner").email("manager@example.com").build();
		when(events.requireUser(nonOwner.getEmail())).thenReturn(nonOwner);
		when(events.requireOwnedEvent(event.getId(), nonOwner)).thenThrow(new AppException(
				pl.backend.weddinggallery.event.exception.EventErrorCode.EVENT_OWNER_REQUIRED));
		assertThatThrownBy(() -> service.remove(event.getId(), "membership-id", nonOwner.getEmail()))
				.isInstanceOf(AppException.class);
	}

	private EventMembership membership(String id, User user, LocalDateTime removedAt) {
		return EventMembership.builder().id(id).event(event).user(user).role(EventRole.MANAGER)
				.joinedAt(LocalDateTime.now().minusDays(2)).removedAt(removedAt).build();
	}

	private void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, MembershipErrorCode code) {
		assertThatThrownBy(call).isInstanceOfSatisfying(AppException.class,
				error -> assertThat(error.getErrorCode()).isEqualTo(code));
	}
}
