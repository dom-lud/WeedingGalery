package pl.backend.weddinggallery.audit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
public class AuditEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false)
	private EventType eventType;

	@Column(name = "user_email", nullable = false)
	private String userEmail;

	@Column(name = "details")
	private String details;

	@Column(name = "event_id", length = 36)
	private String eventId;

	@Column(name = "target_user_id", length = 36)
	private String targetUserId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public AuditEvent(EventType eventType, String userEmail, String details) {
		this.eventType = eventType;
		this.userEmail = userEmail;
		this.details = details;
	}

	public AuditEvent(EventType eventType, String userEmail, String eventId, String targetUserId, String details) {
		this.eventType = eventType;
		this.userEmail = userEmail;
		this.eventId = eventId;
		this.targetUserId = targetUserId;
		this.details = details;
	}
}
