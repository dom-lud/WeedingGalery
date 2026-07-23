package pl.backend.weddinggallery.media.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "media_thumbnails", uniqueConstraints = @UniqueConstraint(name = "uk_media_thumbnail_variant", columnNames = {
		"media_file_id", "variant"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaThumbnail {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "media_file_id", nullable = false)
	private MediaFile mediaFile;
	@Enumerated(EnumType.STRING)
	@Column(length = 40, nullable = false)
	private MediaThumbnailVariant variant;
	@Column(name = "storage_key", length = 500, nullable = false, unique = true)
	private String storageKey;
	@Column(nullable = false)
	private int width;
	@Column(nullable = false)
	private int height;
	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void create() {
		if (id == null)
			id = UUID.randomUUID().toString();
		if (variant == null)
			variant = MediaThumbnailVariant.SMALL;
		createdAt = LocalDateTime.now();
	}
}
