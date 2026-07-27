package pl.backend.weddinggallery.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Notification {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationAudience audience;
	@Column(name = "notification_type", nullable = false, length = 50)
	private String notificationType;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationSeverity severity;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NotificationStatus status;
	@Column(name = "dedupe_key", nullable = false, unique = true, length = 255)
	private String dedupeKey;
	@Column(nullable = false, length = 200)
	private String title;
	@Column(nullable = false, length = 1000)
	private String message;
	@Column(name = "resource_type", length = 50)
	private String resourceType;
	@Column(name = "resource_id", length = 36)
	private String resourceId;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "acknowledged_at")
	private LocalDateTime acknowledgedAt;
	@Column(name = "acknowledged_by")
	private String acknowledgedBy;
	@Version
	private long version;

	@PrePersist
	void create() {
		if (id == null)
			id = UUID.randomUUID().toString();
		if (createdAt == null)
			createdAt = LocalDateTime.now();
		if (status == null)
			status = NotificationStatus.OPEN;
	}
}
