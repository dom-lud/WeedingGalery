package pl.backend.weddinggallery.event.dto;

import java.time.Instant;
import java.time.LocalDate;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.model.EventType;
import pl.backend.weddinggallery.event.model.PrivacyMode;
import pl.backend.weddinggallery.membership.model.EventRole;

public record EventResponse(String id, String name, EventType type, LocalDate eventDate, String description,
		EventStatus status, PrivacyMode privacyMode, EventRole currentUserRole, Instant createdAt, Instant updatedAt) {
}
