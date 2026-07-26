package pl.backend.weddinggallery.media.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.upload.model.UploadSession;

@Entity
@Table(name = "media_files", uniqueConstraints = @UniqueConstraint(name = "uk_media_session_client_file", columnNames = {
		"upload_session_id", "client_file_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "upload_session_id", nullable = false)
	private UploadSession uploadSession;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id", nullable = false)
	private Gallery gallery;
	@Column(name = "client_file_id", length = 100, nullable = false)
	private String clientFileId;
	@Column(name = "original_filename", nullable = false)
	private String originalFilename;
	@Column(name = "storage_key", length = 500, nullable = false, unique = true)
	private String storageKey;
	@Column(name = "expected_size_bytes", nullable = false)
	private long expectedSizeBytes;
	@Column(name = "size_bytes")
	private Long sizeBytes;
	@Column(name = "declared_content_type", length = 100, nullable = false)
	private String declaredContentType;
	@Column(name = "detected_content_type", length = 100)
	private String detectedContentType;
	@Enumerated(EnumType.STRING)
	@Column(name = "media_type", nullable = false)
	private MediaType mediaType;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MediaStatus status;
	@Enumerated(EnumType.STRING)
	@Column(name = "publication_status", nullable = false)
	private PublicationStatus publicationStatus;
	@Column(name = "checksum_sha256", length = 64)
	private String checksumSha256;
	@Column(name = "failure_code", length = 100)
	private String failureCode;
	@Column(name = "width")
	private Integer width;
	@Column(name = "height")
	private Integer height;
	@Column(name = "stored_at")
	private LocalDateTime storedAt;
	@Column(name = "processed_at")
	private LocalDateTime processedAt;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	@Version
	private long version;
	@PrePersist
	void create() {
		if (id == null)
			id = UUID.randomUUID().toString();
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
		if (status == null)
			status = MediaStatus.PENDING;
		if (publicationStatus == null)
			publicationStatus = gallery != null
					&& gallery.getModerationMode() == pl.backend.weddinggallery.gallery.model.ModerationMode.NONE
							? PublicationStatus.APPROVED
							: PublicationStatus.PENDING;
	}
	@PreUpdate
	void update() {
		updatedAt = LocalDateTime.now();
	}
}
