package pl.backend.weddinggallery.gallery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import pl.backend.weddinggallery.event.model.Event;

@Entity
@Table(name = "galleries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gallery {

	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GalleryStatus status;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

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
			status = GalleryStatus.ACTIVE;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
