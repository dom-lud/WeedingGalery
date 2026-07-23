package pl.backend.weddinggallery.media.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.media.repository.*;
import pl.backend.weddinggallery.storage.StorageService;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.media.processing.worker-enabled", havingValue = "true", matchIfMissing = true)
public class MediaProcessingWorker {
	private static final String METADATA_FAILED = "MEDIA_PROCESSING_METADATA_FAILED";
	private static final String THUMBNAIL_FAILED = "MEDIA_PROCESSING_THUMBNAIL_FAILED";
	private static final String STORAGE_FAILED = "MEDIA_PROCESSING_STORAGE_FAILED";
	private static final String UNSUPPORTED_FORMAT = "MEDIA_PROCESSING_UNSUPPORTED_FORMAT";
	private static final String RETRY_EXHAUSTED = "MEDIA_PROCESSING_RETRY_EXHAUSTED";
	private final MediaProcessingJobRepository jobs;
	private final MediaFileRepository mediaFiles;
	private final MediaThumbnailRepository thumbnails;
	private final StorageService storage;
	private final AuditService auditService;
	private final PlatformTransactionManager transactionManager;
	@Value("${app.media.processing.batch-size:2}")
	private int batchSize;
	@Value("${app.media.processing.thumbnail.max-side:512}")
	private int thumbnailMaxSide;
	@Value("${app.media.processing.worker-id:wedding-gallery-worker}")
	private String workerId;

	@Scheduled(fixedDelayString = "${app.media.processing.interval-ms:30000}", initialDelayString = "${app.media.processing.initial-delay-ms:10000}")
	public void processDueJobs() {
		for (int processed = 0; processed < Math.max(1, batchSize); processed++) {
			ClaimedJob claim = tx(this::claimNext);
			if (claim == null)
				return;
			processClaim(claim);
		}
	}

	private void processClaim(ClaimedJob claim) {
		try {
			processMedia(claim.mediaId());
			ProcessingAuditDetails audit = tx(() -> succeed(claim.jobId()));
			auditProcessingOutcome(EventType.MEDIA_PROCESSING_SUCCEEDED, audit);
			log.info("Media processing job succeeded jobId={} mediaId={} galleryId={} attempts={}", audit.jobId(),
					audit.mediaId(), audit.galleryId(), audit.attemptCount());
		} catch (MediaProcessingException ex) {
			ProcessingAuditDetails audit = tx(() -> fail(claim.jobId(), ex.code(), ex.getMessage(), ex.retryable()));
			logFailureOutcome(audit);
		} catch (RuntimeException ex) {
			log.warn("Unexpected media processing failure jobId={} mediaId={}", claim.jobId(), claim.mediaId(), ex);
			ProcessingAuditDetails audit = tx(
					() -> fail(claim.jobId(), STORAGE_FAILED, "Media processing failed.", true));
			logFailureOutcome(audit);
		}
	}

	ClaimedJob claimNext() {
		List<MediaProcessingJob> due = jobs.findDueForUpdate(
				List.of(MediaProcessingJobStatus.PENDING, MediaProcessingJobStatus.RETRY_SCHEDULED),
				LocalDateTime.now(), PageRequest.of(0, 1));
		if (due.isEmpty())
			return null;
		MediaProcessingJob job = due.getFirst();
		MediaFile media = job.getMediaFile();
		job.setStatus(MediaProcessingJobStatus.RUNNING);
		job.setAttemptCount(job.getAttemptCount() + 1);
		job.setStartedAt(LocalDateTime.now());
		job.setLockedAt(LocalDateTime.now());
		job.setLockedBy(workerId);
		job.setLastErrorCode(null);
		job.setLastErrorMessage(null);
		if (media.getStatus() == MediaStatus.STORED)
			media.setStatus(MediaStatus.PROCESSING);
		log.info("Media processing job claimed jobId={} mediaId={} galleryId={} attempt={} workerId={}", job.getId(),
				media.getId(), media.getGallery().getId(), job.getAttemptCount(), workerId);
		return new ClaimedJob(job.getId(), media.getId());
	}

	private void processMedia(String mediaId) {
		MediaFile media = mediaFiles.findById(mediaId)
				.orElseThrow(() -> new MediaProcessingException(METADATA_FAILED, "Media file was not found.", false));
		if (media.getMediaType() == MediaType.VIDEO) {
			markVideoProcessed(media);
			return;
		}
		processImage(media);
	}

	private void processImage(MediaFile media) {
		BufferedImage original = readImage(media);
		media.setWidth(original.getWidth());
		media.setHeight(original.getHeight());
		StoredThumbnail storedThumbnail = thumbnails
				.findByMediaFileIdAndVariant(media.getId(), MediaThumbnailVariant.SMALL)
				.filter(thumbnail -> storage.exists(thumbnail.getStorageKey()))
				.map(thumbnail -> new StoredThumbnail(thumbnail.getStorageKey(), thumbnail.getWidth(),
						thumbnail.getHeight(), thumbnail.getSizeBytes()))
				.orElseGet(() -> storeSmallThumbnail(media, original));
		upsertThumbnail(media, storedThumbnail);
		media.setStatus(MediaStatus.PROCESSED);
		media.setFailureCode(null);
		media.setProcessedAt(LocalDateTime.now());
		mediaFiles.save(media);
	}

	private void markVideoProcessed(MediaFile media) {
		media.setStatus(MediaStatus.PROCESSED);
		media.setFailureCode(null);
		media.setProcessedAt(LocalDateTime.now());
		mediaFiles.save(media);
	}

	private BufferedImage readImage(MediaFile media) {
		try (InputStream input = storage.open(media.getStorageKey())) {
			BufferedImage image = ImageIO.read(input);
			if (image == null)
				throw new MediaProcessingException(UNSUPPORTED_FORMAT, "Image format is not supported by processing.",
						false);
			return image;
		} catch (MediaProcessingException ex) {
			throw ex;
		} catch (RuntimeException | IOException ex) {
			throw new MediaProcessingException(METADATA_FAILED, "Image metadata could not be extracted.", true);
		}
	}

	private StoredThumbnail storeSmallThumbnail(MediaFile media, BufferedImage original) {
		try {
			Dimension size = scaledSize(original.getWidth(), original.getHeight());
			BufferedImage thumbnail = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = thumbnail.createGraphics();
			try {
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				graphics.drawImage(original, 0, 0, size.width, size.height, Color.WHITE, null);
			} finally {
				graphics.dispose();
			}
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (!ImageIO.write(thumbnail, "jpg", output))
				throw new MediaProcessingException(THUMBNAIL_FAILED, "Thumbnail encoder is unavailable.", false);
			byte[] bytes = output.toByteArray();
			String key = thumbnailKey(media, MediaThumbnailVariant.SMALL);
			if (!storage.exists(key))
				storage.save(key, new ByteArrayInputStream(bytes), bytes.length);
			return new StoredThumbnail(key, size.width, size.height, bytes.length);
		} catch (MediaProcessingException ex) {
			throw ex;
		} catch (RuntimeException | IOException ex) {
			throw new MediaProcessingException(THUMBNAIL_FAILED, "Thumbnail could not be generated.", true);
		}
	}

	private Dimension scaledSize(int width, int height) {
		int maxSide = Math.max(1, thumbnailMaxSide);
		if (width <= maxSide && height <= maxSide)
			return new Dimension(width, height);
		double scale = Math.min((double) maxSide / width, (double) maxSide / height);
		return new Dimension(Math.max(1, (int) Math.round(width * scale)),
				Math.max(1, (int) Math.round(height * scale)));
	}

	private void upsertThumbnail(MediaFile media, StoredThumbnail stored) {
		MediaThumbnail thumbnail = thumbnails.findByMediaFileIdAndVariant(media.getId(), MediaThumbnailVariant.SMALL)
				.orElseGet(() -> MediaThumbnail.builder().mediaFile(media).variant(MediaThumbnailVariant.SMALL)
						.storageKey(stored.key()).build());
		thumbnail.setStorageKey(stored.key());
		thumbnail.setWidth(stored.width());
		thumbnail.setHeight(stored.height());
		thumbnail.setSizeBytes(stored.sizeBytes());
		thumbnails.save(thumbnail);
	}

	private String thumbnailKey(MediaFile media, MediaThumbnailVariant variant) {
		String suffix = "/thumbnails/" + variant.name().toLowerCase() + ".jpg";
		if (media.getStorageKey().endsWith("/original"))
			return media.getStorageKey().substring(0, media.getStorageKey().length() - "/original".length()) + suffix;
		return media.getStorageKey() + suffix;
	}

	ProcessingAuditDetails succeed(String jobId) {
		MediaProcessingJob job = jobs.findById(jobId).orElseThrow();
		MediaFile media = job.getMediaFile();
		job.setStatus(MediaProcessingJobStatus.SUCCEEDED);
		job.setFinishedAt(LocalDateTime.now());
		job.setLockedAt(null);
		job.setLockedBy(null);
		return auditDetails(job, media, null);
	}

	ProcessingAuditDetails fail(String jobId, String code, String message, boolean retryable) {
		MediaProcessingJob job = jobs.findById(jobId).orElseThrow();
		MediaFile media = job.getMediaFile();
		job.setLastErrorCode(code);
		job.setLastErrorMessage(message);
		job.setLockedAt(null);
		job.setLockedBy(null);
		if (retryable && job.getAttemptCount() < job.getMaxAttempts()) {
			job.setStatus(MediaProcessingJobStatus.RETRY_SCHEDULED);
			job.setScheduledAt(LocalDateTime.now().plusSeconds((long) Math.pow(2, job.getAttemptCount()) * 10L));
			return auditDetails(job, media, code);
		}
		job.setStatus(retryable ? MediaProcessingJobStatus.MANUAL_REVIEW : MediaProcessingJobStatus.FAILED);
		job.setFinishedAt(LocalDateTime.now());
		media.setStatus(MediaStatus.PROCESSING_FAILED);
		media.setFailureCode(retryable ? RETRY_EXHAUSTED : code);
		media.setProcessedAt(LocalDateTime.now());
		return auditDetails(job, media, media.getFailureCode());
	}

	private void logFailureOutcome(ProcessingAuditDetails audit) {
		if (audit.terminal()) {
			auditProcessingOutcome(EventType.MEDIA_PROCESSING_FAILED, audit);
			log.warn("Media processing job failed jobId={} mediaId={} galleryId={} status={} code={} attempts={}",
					audit.jobId(), audit.mediaId(), audit.galleryId(), audit.jobStatus(), audit.failureCode(),
					audit.attemptCount());
			return;
		}
		log.info(
				"Media processing job retry scheduled jobId={} mediaId={} galleryId={} code={} attempt={} maxAttempts={}",
				audit.jobId(), audit.mediaId(), audit.galleryId(), audit.failureCode(), audit.attemptCount(),
				audit.maxAttempts());
	}

	private void auditProcessingOutcome(EventType eventType, ProcessingAuditDetails audit) {
		auditService.logSystemGalleryEvent(eventType, audit.eventId(), audit.galleryId(),
				"mediaId=" + audit.mediaId() + ",jobId=" + audit.jobId() + ",status=" + audit.jobStatus() + ",attempts="
						+ audit.attemptCount() + (audit.failureCode() == null ? "" : ",code=" + audit.failureCode()));
	}

	private ProcessingAuditDetails auditDetails(MediaProcessingJob job, MediaFile media, String failureCode) {
		return new ProcessingAuditDetails(job.getId(), media.getId(), media.getGallery().getId(),
				media.getGallery().getEvent().getId(), job.getStatus(), job.getAttemptCount(), job.getMaxAttempts(),
				failureCode);
	}

	private <T> T tx(java.util.function.Supplier<T> work) {
		return new TransactionTemplate(transactionManager).execute(status -> work.get());
	}

	private record ClaimedJob(String jobId, String mediaId) {
	}

	private record StoredThumbnail(String key, int width, int height, long sizeBytes) {
	}

	private record ProcessingAuditDetails(String jobId, String mediaId, String galleryId, String eventId,
			MediaProcessingJobStatus jobStatus, int attemptCount, int maxAttempts, String failureCode) {
		private boolean terminal() {
			return jobStatus == MediaProcessingJobStatus.FAILED || jobStatus == MediaProcessingJobStatus.MANUAL_REVIEW;
		}
	}
}
