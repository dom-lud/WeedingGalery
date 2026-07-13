package pl.backend.weddinggallery.upload.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;

@Entity
@Table(name = "upload_sessions", uniqueConstraints = @UniqueConstraint(name = "uk_upload_access_idempotency", columnNames = {
		"public_access_id", "idempotency_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadSession {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id", nullable = false)
	private Gallery gallery;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "public_access_id", nullable = false)
	private GalleryAccess publicAccess;
	@Column(name = "grant_fingerprint", length = 64, nullable = false)
	private String grantFingerprint;
	@Column(name = "idempotency_key", length = 128, nullable = false)
	private String idempotencyKey;
	@Column(name = "request_fingerprint", length = 64, nullable = false)
	private String requestFingerprint;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UploadSessionStatus status;
	@Column(name = "total_files", nullable = false)
	private int totalFiles;
	@Column(name = "total_bytes", nullable = false)
	private long totalBytes;
	@Column(name = "reserved_bytes", nullable = false)
	private long reservedBytes;
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;
	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	@OneToMany(mappedBy = "uploadSession", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<MediaFile> files = new ArrayList<>();
	@Version
	private long version;
	@PrePersist
	void create() {
		if (id == null)
			id = UUID.randomUUID().toString();
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
		if (status == null)
			status = UploadSessionStatus.OPEN;
	}
	@PreUpdate
	void update() {
		updatedAt = LocalDateTime.now();
	}
}
