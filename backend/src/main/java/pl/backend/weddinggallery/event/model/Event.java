package pl.backend.weddinggallery.event.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import pl.backend.weddinggallery.user.model.User;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventType type;

	@Column(name = "event_date")
	private LocalDate eventDate;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_user_id", nullable = false)
	private User owner;

	@Enumerated(EnumType.STRING)
	@Column(name = "privacy_mode", nullable = false)
	private PrivacyMode privacyMode;

	@Column(name = "archived_at")
	private LocalDateTime archivedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Version
	private long version;

	@PrePersist
	protected void onCreate() {
		if (id == null) {
			id = java.util.UUID.randomUUID().toString();
		}
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
		if (status == null) {
			status = EventStatus.DRAFT;
		}
		if (privacyMode == null) {
			privacyMode = PrivacyMode.PRIVATE;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
