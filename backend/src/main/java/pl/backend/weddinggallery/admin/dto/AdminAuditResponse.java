package pl.backend.weddinggallery.admin.dto;

import java.time.LocalDateTime;
import pl.backend.weddinggallery.audit.model.EventType;

public record AdminAuditResponse(Long id, EventType eventType, String actorType, String actorEmail, String resourceType,
		String resourceId, LocalDateTime createdAt, String result) {
}
