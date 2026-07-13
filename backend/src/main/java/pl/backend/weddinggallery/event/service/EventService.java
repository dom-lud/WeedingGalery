package pl.backend.weddinggallery.event.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.dto.EventResponse;
import pl.backend.weddinggallery.event.dto.EventWriteRequest;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class EventService {
	private final EventRepository eventRepository;
	private final EventMembershipRepository membershipRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	@Transactional
	public EventResponse create(EventWriteRequest request, String actorEmail) {
		User actor = requireUser(actorEmail);
		Event event = Event.builder().name(request.name().trim()).type(request.type()).eventDate(request.eventDate())
				.description(normalizeDescription(request.description())).privacyMode(request.privacyMode())
				.owner(actor).status(EventStatus.DRAFT).build();
		eventRepository.save(event);
		auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_CREATED, event.getId(), null, "status=DRAFT");
		return toResponse(event, EventRole.OWNER);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> list(String actorEmail) {
		User actor = requireUser(actorEmail);
		return eventRepository.findAccessibleByUserId(actor.getId()).stream()
				.map(event -> toResponse(event, roleFor(event, actor))).toList();
	}

	@Transactional(readOnly = true)
	public EventResponse get(String eventId, String actorEmail) {
		User actor = requireUser(actorEmail);
		Event event = requireAccessibleEvent(eventId, actor);
		return toResponse(event, roleFor(event, actor));
	}

	@Transactional
	public EventResponse update(String eventId, EventWriteRequest request, String actorEmail) {
		User actor = requireUser(actorEmail);
		Event event = requireAccessibleEvent(eventId, actor);
		if (event.getStatus() == EventStatus.ARCHIVED) {
			throw new AppException(EventErrorCode.EVENT_ARCHIVED);
		}
		event.setName(request.name().trim());
		event.setType(request.type());
		event.setEventDate(request.eventDate());
		event.setDescription(normalizeDescription(request.description()));
		event.setPrivacyMode(request.privacyMode());
		event.setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_UPDATED, event.getId(), null,
				"metadata=updated");
		return toResponse(event, roleFor(event, actor));
	}

	@Transactional
	public EventResponse archive(String eventId, String actorEmail) {
		User actor = requireUser(actorEmail);
		Event event = requireOwnedEvent(eventId, actor);
		if (event.getStatus() != EventStatus.ARCHIVED) {
			event.setStatus(EventStatus.ARCHIVED);
			event.setArchivedAt(LocalDateTime.now());
			event.setUpdatedAt(LocalDateTime.now());
			auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_ARCHIVED, event.getId(), null,
					"status=ARCHIVED");
		}
		return toResponse(event, EventRole.OWNER);
	}

	@Transactional
	public void delete(String eventId, String actorEmail) {
		User actor = requireUser(actorEmail);
		Event event = requireOwnedEvent(eventId, actor);
		event.setStatus(EventStatus.DELETED);
		event.setDeletedAt(LocalDateTime.now());
		event.setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredEvent(actor.getEmail(), EventType.EVENT_DELETED, event.getId(), null, "status=DELETED");
	}

	@Transactional(readOnly = true)
	public User requireUser(String email) {
		return userRepository.findByEmail(email.trim().toLowerCase())
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Event requireAccessibleEvent(String eventId, User actor) {
		return eventRepository.findAccessibleById(eventId, actor.getId())
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Event requireOwnedEvent(String eventId, User actor) {
		Event event = requireAccessibleEvent(eventId, actor);
		if (!event.getOwner().getId().equals(actor.getId())) {
			throw new AppException(EventErrorCode.EVENT_OWNER_REQUIRED);
		}
		return event;
	}

	public EventRole roleFor(Event event, User actor) {
		if (event.getOwner().getId().equals(actor.getId())) {
			return EventRole.OWNER;
		}
		if (membershipRepository.existsByEventIdAndUserIdAndRemovedAtIsNull(event.getId(), actor.getId())) {
			return EventRole.MANAGER;
		}
		throw new AppException(EventErrorCode.EVENT_NOT_FOUND);
	}

	public EventResponse toResponse(Event event, EventRole role) {
		return new EventResponse(event.getId(), event.getName(), event.getType(), event.getEventDate(),
				event.getDescription(), event.getStatus(), event.getPrivacyMode(), role,
				event.getCreatedAt().toInstant(ZoneOffset.UTC), event.getUpdatedAt().toInstant(ZoneOffset.UTC));
	}

	private String normalizeDescription(String description) {
		if (description == null) {
			return null;
		}
		String trimmed = description.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
