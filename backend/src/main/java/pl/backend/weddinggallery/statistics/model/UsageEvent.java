package pl.backend.weddinggallery.statistics.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "usage_events")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UsageEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 40)
	private UsageEventType eventType;
	@Column(name = "event_id", length = 36)
	private String eventId;
	@Column(name = "gallery_id", length = 36)
	private String galleryId;
	@Column(name = "actor_user_id", length = 36)
	private String actorUserId;
	@Column(name = "public_access_id", length = 36)
	private String publicAccessId;
	@Column(nullable = false)
	private long quantity;
	@Column(name = "occurred_at", nullable = false)
	private LocalDateTime occurredAt;

	@PrePersist
	void create() {
		if (quantity == 0)
			quantity = 1;
		if (occurredAt == null)
			occurredAt = LocalDateTime.now();
	}
}
