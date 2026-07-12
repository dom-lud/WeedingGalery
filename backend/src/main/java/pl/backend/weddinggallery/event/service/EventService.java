package pl.backend.weddinggallery.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

	private final EventRepository eventRepository;

	@Transactional(readOnly = true)
	public List<Event> getEventsForUser(String userId) {
		log.debug("Fetching events for user {}", userId);
		return eventRepository.findByOwnerId(userId);
	}

	@Transactional(readOnly = true)
	public Event getEventById(String eventId, String currentUserId) {
		log.debug("Fetching event {} for user {}", eventId, currentUserId);
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));

		if (!event.getOwner().getId().equals(currentUserId)) {
			log.warn("User {} attempted to access event {} without permission", currentUserId, eventId);
			throw new AppException(EventErrorCode.UNAUTHORIZED_ACCESS);
		}

		return event;
	}

	@Transactional
	public Event createEvent(Event event) {
		log.info("Creating new event: {}", event.getName());
		return eventRepository.save(event);
	}
}
