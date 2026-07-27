package pl.backend.weddinggallery.notification.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.notification.dto.AdminNotificationResponse;
import pl.backend.weddinggallery.notification.model.*;
import pl.backend.weddinggallery.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public Page<AdminNotificationResponse> adminOpen(Pageable pageable) {
		return notificationRepository
				.findByAudienceAndStatus(NotificationAudience.ADMIN, NotificationStatus.OPEN, pageable)
				.map(this::toResponse);
	}

	@Transactional
	public AdminNotificationResponse createAdminAlert(String type, NotificationSeverity severity, String dedupeKey,
			String title, String message, String resourceType, String resourceId) {
		Notification notification = notificationRepository.findByDedupeKey(dedupeKey)
				.orElseGet(() -> notificationRepository.save(Notification.builder().audience(NotificationAudience.ADMIN)
						.notificationType(type).severity(severity).status(NotificationStatus.OPEN).dedupeKey(dedupeKey)
						.title(title).message(message).resourceType(resourceType).resourceId(resourceId).build()));
		if (notification.getStatus() == NotificationStatus.ACKNOWLEDGED) {
			notification.setStatus(NotificationStatus.OPEN);
			notification.setAcknowledgedAt(null);
			notification.setAcknowledgedBy(null);
			notification.setSeverity(severity);
			notification.setMessage(message);
		}
		return toResponse(notification);
	}

	@Transactional
	public AdminNotificationResponse acknowledge(String id, String actorEmail) {
		Notification notification = notificationRepository.findById(id)
				.orElseThrow(() -> new NotificationNotFoundException(id));
		if (notification.getStatus() == NotificationStatus.OPEN) {
			notification.setStatus(NotificationStatus.ACKNOWLEDGED);
			notification.setAcknowledgedAt(LocalDateTime.now());
			notification.setAcknowledgedBy(actorEmail);
		}
		return toResponse(notification);
	}

	private AdminNotificationResponse toResponse(Notification n) {
		return new AdminNotificationResponse(n.getId(), n.getNotificationType(), n.getSeverity(), n.getStatus(),
				n.getTitle(), n.getMessage(), n.getResourceType(), n.getResourceId(), n.getCreatedAt(),
				n.getAcknowledgedAt(), n.getAcknowledgedBy());
	}

	public static class NotificationNotFoundException extends RuntimeException {
		public NotificationNotFoundException(String id) {
			super("Notification not found: " + id);
		}
	}
}
