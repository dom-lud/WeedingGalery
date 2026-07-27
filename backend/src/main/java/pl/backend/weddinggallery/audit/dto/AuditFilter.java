package pl.backend.weddinggallery.audit.dto;

import java.time.LocalDateTime;
import pl.backend.weddinggallery.audit.model.EventType;

public record AuditFilter(EventType eventType, String actorType, String eventId, String galleryId, LocalDateTime from,
		LocalDateTime to) {
	public AuditFilter normalized() {
		return new AuditFilter(eventType, normalize(actorType), normalize(eventId), normalize(galleryId), from, to);
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
