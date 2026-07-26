package pl.backend.weddinggallery.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.model.EventType;

public record AdminEventResponse(String id, String name, EventType type, EventStatus status, String ownerUserId,
		String ownerEmail, LocalDate eventDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
