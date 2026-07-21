package pl.backend.weddinggallery.media.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "media_processing_jobs", uniqueConstraints = @UniqueConstraint(name = "uk_media_processing_job_type", columnNames = {
		"media_file_id", "job_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaProcessingJob {
	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "media_file_id", nullable = false)
	private MediaFile mediaFile;
	@Enumerated(EnumType.STRING)
	@Column(name = "job_type", length = 40, nullable = false)
	private MediaProcessingJobType jobType;
	@Enumerated(EnumType.STRING)
	@Column(length = 40, nullable = false)
	private MediaProcessingJobStatus status;
	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;
	@Column(name = "max_attempts", nullable = false)
	private int maxAttempts;
	@Column(name = "last_error_code", length = 100)
	private String lastErrorCode;
	@Column(name = "last_error_message", length = 500)
	private String lastErrorMessage;
	@Column(name = "scheduled_at", nullable = false)
	private LocalDateTime scheduledAt;
	@Column(name = "started_at")
	private LocalDateTime startedAt;
	@Column(name = "finished_at")
	private LocalDateTime finishedAt;
	@Column(name = "locked_at")
	private LocalDateTime lockedAt;
	@Column(name = "locked_by", length = 100)
	private String lockedBy;
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
		if (status == null)
			status = MediaProcessingJobStatus.PENDING;
		if (jobType == null)
			jobType = MediaProcessingJobType.PROCESS_MEDIA;
		if (maxAttempts == 0)
			maxAttempts = 3;
		if (scheduledAt == null)
			scheduledAt = LocalDateTime.now();
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void update() {
		updatedAt = LocalDateTime.now();
	}
}
