package pl.backend.weddinggallery.publicaccess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import pl.backend.weddinggallery.gallery.model.Gallery;

@Entity
@Table(name = "gallery_accesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryAccess {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id", nullable = false)
	private Gallery gallery;
	@Column(name = "token_hash", length = 64, nullable = false, unique = true)
	private String tokenHash;
	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Version
	private long version;

	@PrePersist
	void onCreate() {
		if (id == null)
			id = UUID.randomUUID().toString();
		if (createdAt == null)
			createdAt = LocalDateTime.now();
	}
}
