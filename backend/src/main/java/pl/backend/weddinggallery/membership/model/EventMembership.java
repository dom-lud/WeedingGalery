package pl.backend.weddinggallery.membership.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.user.model.User;

@Entity
@Table(name = "event_memberships", uniqueConstraints = @UniqueConstraint(name = "uk_event_membership_event_user", columnNames = {
		"event_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMembership {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventRole role;

	@Column(name = "joined_at", nullable = false)
	private LocalDateTime joinedAt;

	@Column(name = "removed_at")
	private LocalDateTime removedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Version
	private long version;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		if (role == null) {
			role = EventRole.MANAGER;
		}
		if (joinedAt == null) {
			joinedAt = now;
		}
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public boolean isActive() {
		return removedAt == null;
	}

	public void reactivate() {
		role = EventRole.MANAGER;
		removedAt = null;
		joinedAt = LocalDateTime.now();
	}

	public void remove() {
		removedAt = LocalDateTime.now();
	}
}
