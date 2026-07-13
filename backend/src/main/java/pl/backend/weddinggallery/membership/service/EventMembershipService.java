package pl.backend.weddinggallery.membership.service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.dto.EventResponse;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.membership.dto.AddEventManagerRequest;
import pl.backend.weddinggallery.membership.dto.EventMemberResponse;
import pl.backend.weddinggallery.membership.dto.OwnershipTransferRequest;
import pl.backend.weddinggallery.membership.exception.MembershipErrorCode;
import pl.backend.weddinggallery.membership.model.EventMembership;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class EventMembershipService {
	private final EventMembershipRepository membershipRepository;
	private final UserRepository userRepository;
	private final EventService eventService;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public List<EventMemberResponse> list(String eventId, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireAccessibleEvent(eventId, actor);
		List<EventMemberResponse> result = new ArrayList<>();
		result.add(new EventMemberResponse(null, event.getOwner().getId(), event.getOwner().getEmail(), EventRole.OWNER,
				event.getCreatedAt().toInstant(ZoneOffset.UTC)));
		membershipRepository.findByEventIdAndRemovedAtIsNullOrderByJoinedAtAsc(eventId).stream().map(this::toResponse)
				.forEach(result::add);
		return result;
	}

	@Transactional
	public EventMemberResponse add(String eventId, AddEventManagerRequest request, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireOwnedEvent(eventId, actor);
		if (request.role() != EventRole.MANAGER) {
			throw new AppException(MembershipErrorCode.INVALID_MEMBERSHIP_TARGET);
		}
		User target = userRepository.findByEmail(request.email().trim().toLowerCase())
				.orElseThrow(() -> new AppException(MembershipErrorCode.USER_NOT_FOUND));
		if (event.getOwner().getId().equals(target.getId())) {
			throw new AppException(MembershipErrorCode.INVALID_MEMBERSHIP_TARGET);
		}
		EventMembership membership = membershipRepository.findByEventIdAndUserId(eventId, target.getId())
				.map(existing -> {
					if (existing.isActive()) {
						throw new AppException(MembershipErrorCode.MEMBERSHIP_ALREADY_ACTIVE);
					}
					existing.reactivate();
					return existing;
				}).orElseGet(() -> EventMembership.builder().event(event).user(target).role(EventRole.MANAGER).build());
		membershipRepository.save(membership);
		auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_MANAGER_ADDED, eventId, target.getId(),
				"role=MANAGER");
		return toResponse(membership);
	}

	@Transactional
	public void remove(String eventId, String membershipId, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireOwnedEvent(eventId, actor);
		EventMembership membership = membershipRepository.findByIdAndEventIdAndRemovedAtIsNull(membershipId, eventId)
				.orElseThrow(() -> new AppException(MembershipErrorCode.MEMBERSHIP_NOT_FOUND));
		membership.remove();
		auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_MANAGER_REMOVED, event.getId(),
				membership.getUser().getId(), "role=MANAGER");
	}

	@Transactional
	public EventResponse transferOwnership(String eventId, OwnershipTransferRequest request, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireOwnedEvent(eventId, actor);
		EventMembership targetMembership = membershipRepository
				.findByIdAndEventIdAndRemovedAtIsNull(request.targetMembershipId(), eventId)
				.orElseThrow(() -> new AppException(MembershipErrorCode.MEMBERSHIP_NOT_FOUND));
		User newOwner = targetMembership.getUser();
		targetMembership.remove();

		EventMembership oldOwnerMembership = membershipRepository.findByEventIdAndUserId(eventId, actor.getId())
				.map(existing -> {
					existing.reactivate();
					return existing;
				}).orElseGet(() -> EventMembership.builder().event(event).user(actor).role(EventRole.MANAGER).build());
		membershipRepository.save(oldOwnerMembership);
		event.setOwner(newOwner);
		event.setUpdatedAt(java.time.LocalDateTime.now());
		auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_OWNERSHIP_TRANSFERRED, eventId,
				newOwner.getId(), "oldRole=OWNER;newRole=MANAGER;targetRole=OWNER");
		return eventService.toResponse(event, EventRole.MANAGER);
	}

	private EventMemberResponse toResponse(EventMembership membership) {
		return new EventMemberResponse(membership.getId(), membership.getUser().getId(),
				membership.getUser().getEmail(), membership.getRole(),
				membership.getJoinedAt().toInstant(ZoneOffset.UTC));
	}
}
