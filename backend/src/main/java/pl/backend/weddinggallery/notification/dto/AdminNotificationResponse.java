package pl.backend.weddinggallery.notification.dto;

import java.time.LocalDateTime;
import pl.backend.weddinggallery.notification.model.NotificationSeverity;
import pl.backend.weddinggallery.notification.model.NotificationStatus;

public record AdminNotificationResponse(String id, String type, NotificationSeverity severity,
		NotificationStatus status, String title, String message, String resourceType, String resourceId,
		LocalDateTime createdAt, LocalDateTime acknowledgedAt, String acknowledgedBy) {
}
