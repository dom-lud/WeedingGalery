package pl.backend.weddinggallery.media.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.*;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.media.repository.*;
import pl.backend.weddinggallery.storage.*;

@ExtendWith(MockitoExtension.class)
class MediaProcessingWorkerTest {
	@Mock
	MediaProcessingJobRepository jobs;
	@Mock
	MediaFileRepository mediaFiles;
	@Mock
	MediaThumbnailRepository thumbnails;
	@Mock
	StorageService storage;
	@Mock
	AuditService auditService;
	@Mock
	PlatformTransactionManager transactions;
	@Mock
	TransactionStatus transactionStatus;
	private MediaProcessingWorker worker;
	private MediaFile media;
	private MediaProcessingJob job;

	@BeforeEach
	void setUp() {
		worker = new MediaProcessingWorker(jobs, mediaFiles, thumbnails, storage, auditService, transactions);
		ReflectionTestUtils.setField(worker, "batchSize", 1);
		ReflectionTestUtils.setField(worker, "thumbnailMaxSide", 64);
		ReflectionTestUtils.setField(worker, "workerId", "test-worker");
		lenient().when(transactions.getTransaction(any())).thenReturn(transactionStatus);
		Gallery gallery = Gallery.builder().id("gallery").event(Event.builder().id("event").build()).build();
		media = MediaFile.builder().id("media").gallery(gallery).storageKey("events/e/galleries/g/media/media/original")
				.mediaType(MediaType.IMAGE).status(MediaStatus.STORED).build();
		job = MediaProcessingJob.builder().id("job").mediaFile(media).jobType(MediaProcessingJobType.PROCESS_MEDIA)
				.status(MediaProcessingJobStatus.PENDING).attemptCount(0).maxAttempts(3)
				.scheduledAt(LocalDateTime.now()).build();
	}

	@Test
	void processesImageIdempotentlyAndStoresSmallThumbnailWithoutLeakingOriginalPath() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original"))
				.thenReturn(new ByteArrayInputStream(jpeg(200, 100)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.save(contains("/thumbnails/small.jpg"), any(), anyLong()))
				.thenReturn(new StoredObject(640L, "checksum"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSED);
		assertThat(media.getWidth()).isEqualTo(200);
		assertThat(media.getHeight()).isEqualTo(100);
		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.SUCCEEDED);
		verify(auditService).logSystemGalleryEvent(eq(EventType.MEDIA_PROCESSING_SUCCEEDED), eq("event"), eq("gallery"),
				contains("mediaId=media,jobId=job,status=SUCCEEDED,attempts=1"));
		ArgumentCaptor<MediaThumbnail> saved = ArgumentCaptor.forClass(MediaThumbnail.class);
		verify(thumbnails).save(saved.capture());
		assertThat(saved.getValue().getStorageKey()).isEqualTo("events/e/galleries/g/media/media/thumbnails/small.jpg");
		assertThat(saved.getValue().getWidth()).isEqualTo(64);
		assertThat(saved.getValue().getHeight()).isEqualTo(32);
	}

	@Test
	void returnsWithoutWorkWhenNoJobIsDue() {
		when(jobs.findDueForUpdate(anyCollection(), any(), any())).thenReturn(List.of());

		worker.processDueJobs();

		verifyNoInteractions(mediaFiles, thumbnails, storage);
	}

	@Test
	void processesVideoWithoutGeneratingPreviewInFirstIteration() {
		media.setMediaType(MediaType.VIDEO);
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSED);
		assertThat(media.getProcessedAt()).isNotNull();
		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.SUCCEEDED);
		verifyNoInteractions(thumbnails, storage);
	}

	@Test
	void reusesExistingThumbnailRecordWhenStorageObjectExists() {
		MediaThumbnail existing = MediaThumbnail.builder().mediaFile(media).variant(MediaThumbnailVariant.SMALL)
				.storageKey("events/e/galleries/g/media/media/thumbnails/small.jpg").width(40).height(20)
				.sizeBytes(123L).build();
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original"))
				.thenReturn(new ByteArrayInputStream(jpeg(80, 40)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL))
				.thenReturn(Optional.of(existing));
		when(storage.exists("events/e/galleries/g/media/media/thumbnails/small.jpg")).thenReturn(true);
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSED);
		verify(storage, never()).save(anyString(), any(), anyLong());
		verify(thumbnails).save(existing);
	}

	@Test
	void keepsSmallImagesAtOriginalThumbnailSize() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original"))
				.thenReturn(new ByteArrayInputStream(jpeg(20, 10)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.save(contains("/thumbnails/small.jpg"), any(), anyLong()))
				.thenReturn(new StoredObject(100L, "checksum"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		ArgumentCaptor<MediaThumbnail> saved = ArgumentCaptor.forClass(MediaThumbnail.class);
		verify(thumbnails).save(saved.capture());
		assertThat(saved.getValue().getWidth()).isEqualTo(20);
		assertThat(saved.getValue().getHeight()).isEqualTo(10);
	}

	@Test
	void retryableFailureIsRescheduledBeforeAttemptLimit() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original")).thenThrow(new RuntimeException("disk"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.RETRY_SCHEDULED);
		assertThat(job.getAttemptCount()).isEqualTo(1);
		assertThat(job.getScheduledAt()).isAfter(LocalDateTime.now());
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSING);
		verify(auditService, never()).logSystemGalleryEvent(any(), any(), any(), any());
	}

	@Test
	void claimKeepsAlreadyProcessedMediaStatus() {
		media.setStatus(MediaStatus.PROCESSED);
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original"))
				.thenReturn(new ByteArrayInputStream(jpeg(20, 10)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.save(contains("/thumbnails/small.jpg"), any(), anyLong()))
				.thenReturn(new StoredObject(100L, "checksum"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.SUCCEEDED);
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSED);
	}

	@Test
	void thumbnailKeyFallsBackWhenOriginalSuffixIsMissing() {
		media.setStorageKey("events/e/custom-key");
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/custom-key")).thenReturn(new ByteArrayInputStream(jpeg(20, 10)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.save(eq("events/e/custom-key/thumbnails/small.jpg"), any(), anyLong()))
				.thenReturn(new StoredObject(100L, "checksum"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		ArgumentCaptor<MediaThumbnail> saved = ArgumentCaptor.forClass(MediaThumbnail.class);
		verify(thumbnails).save(saved.capture());
		assertThat(saved.getValue().getStorageKey()).isEqualTo("events/e/custom-key/thumbnails/small.jpg");
	}

	@Test
	void missingMediaFileFailsPermanentlyWithoutRetry() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.empty());
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.FAILED);
		assertThat(job.getLastErrorCode()).isEqualTo("MEDIA_PROCESSING_METADATA_FAILED");
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSING_FAILED);
		verify(auditService).logSystemGalleryEvent(eq(EventType.MEDIA_PROCESSING_FAILED), eq("event"), eq("gallery"),
				contains("code=MEDIA_PROCESSING_METADATA_FAILED"));
	}

	@Test
	void replaysThumbnailSaveWhenObjectAlreadyExistsButDatabaseRowWasNotCommitted() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original"))
				.thenReturn(new ByteArrayInputStream(jpeg(200, 100)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.exists("events/e/galleries/g/media/media/thumbnails/small.jpg")).thenReturn(true);
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.SUCCEEDED);
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSED);
		verify(storage, never()).save(anyString(), any(), anyLong());
		ArgumentCaptor<MediaThumbnail> saved = ArgumentCaptor.forClass(MediaThumbnail.class);
		verify(thumbnails).save(saved.capture());
		assertThat(saved.getValue().getStorageKey()).isEqualTo("events/e/galleries/g/media/media/thumbnails/small.jpg");
	}

	@Test
	void retryableFailureMovesToManualReviewAfterLimitAndKeepsOriginal() {
		job.setMaxAttempts(1);
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original")).thenThrow(new RuntimeException("disk"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.MANUAL_REVIEW);
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSING_FAILED);
		assertThat(media.getFailureCode()).isEqualTo("MEDIA_PROCESSING_RETRY_EXHAUSTED");
		verify(storage, never()).delete(anyString());
		verify(auditService).logSystemGalleryEvent(eq(EventType.MEDIA_PROCESSING_FAILED), eq("event"), eq("gallery"),
				contains("code=MEDIA_PROCESSING_RETRY_EXHAUSTED"));
	}

	@Test
	void unsupportedImageFailsPermanentlyWithoutRetry() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open("events/e/galleries/g/media/media/original"))
				.thenReturn(new ByteArrayInputStream("not an image".getBytes()));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.FAILED);
		assertThat(job.getLastErrorCode()).isEqualTo("MEDIA_PROCESSING_UNSUPPORTED_FORMAT");
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSING_FAILED);
		assertThat(media.getFailureCode()).isEqualTo("MEDIA_PROCESSING_UNSUPPORTED_FORMAT");
		verify(storage, never()).save(contains("thumbnail"), any(), anyLong());
	}

	@Test
	void storageFailureWhileWritingThumbnailIsRetryableAndLeavesMediaProcessing() {
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open(media.getStorageKey())).thenReturn(new ByteArrayInputStream(jpeg(200, 100)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.save(contains("/thumbnails/small.jpg"), any(), anyLong()))
				.thenThrow(new RuntimeException("storage unavailable"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.RETRY_SCHEDULED);
		assertThat(job.getLastErrorCode()).isEqualTo("MEDIA_PROCESSING_THUMBNAIL_FAILED");
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSING);
		verify(auditService, never()).logSystemGalleryEvent(any(), any(), any(), any());
	}

	@Test
	void retryableFailureAtAttemptLimitBecomesManualReviewAndAuditsExhaustion() {
		job.setAttemptCount(1);
		job.setMaxAttempts(2);
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open(media.getStorageKey())).thenThrow(new RuntimeException("disk"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.MANUAL_REVIEW);
		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSING_FAILED);
		assertThat(media.getFailureCode()).isEqualTo("MEDIA_PROCESSING_RETRY_EXHAUSTED");
		verify(auditService).logSystemGalleryEvent(eq(EventType.MEDIA_PROCESSING_FAILED), eq("event"), eq("gallery"),
				contains("code=MEDIA_PROCESSING_RETRY_EXHAUSTED"));
	}

	@Test
	void missingStoredThumbnailRegeneratesWhenDatabaseRowPointsToDeletedObject() {
		MediaThumbnail stale = MediaThumbnail.builder().mediaFile(media).variant(MediaThumbnailVariant.SMALL)
				.storageKey("events/e/galleries/g/media/media/thumbnails/small.jpg").width(12).height(6).sizeBytes(1L)
				.build();
		stubDueJob();
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open(media.getStorageKey())).thenReturn(new ByteArrayInputStream(jpeg(80, 40)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL))
				.thenReturn(Optional.of(stale));
		when(storage.exists(stale.getStorageKey())).thenReturn(false);
		when(storage.save(eq(stale.getStorageKey()), any(), anyLong())).thenReturn(new StoredObject(500L, "checksum"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(media.getStatus()).isEqualTo(MediaStatus.PROCESSED);
		assertThat(stale.getWidth()).isEqualTo(64);
		assertThat(stale.getHeight()).isEqualTo(32);
		verify(storage).save(eq(stale.getStorageKey()), any(), anyLong());
	}

	@Test
	void batchSizeProcessesOnlyConfiguredNumberOfJobs() {
		MediaFile secondMedia = MediaFile.builder().id("media-2").gallery(media.getGallery()).storageKey("second")
				.mediaType(MediaType.VIDEO).status(MediaStatus.STORED).build();
		MediaProcessingJob secondJob = MediaProcessingJob.builder().id("job-2").mediaFile(secondMedia)
				.jobType(MediaProcessingJobType.PROCESS_MEDIA).status(MediaProcessingJobStatus.PENDING).attemptCount(0)
				.maxAttempts(3).scheduledAt(LocalDateTime.now()).build();
		ReflectionTestUtils.setField(worker, "batchSize", 1);
		when(jobs.findDueForUpdate(anyCollection(), any(), any())).thenReturn(List.of(job));
		when(mediaFiles.findById("media")).thenReturn(Optional.of(media));
		when(storage.open(media.getStorageKey())).thenReturn(new ByteArrayInputStream(jpeg(20, 10)));
		when(thumbnails.findByMediaFileIdAndVariant("media", MediaThumbnailVariant.SMALL)).thenReturn(Optional.empty());
		when(storage.save(anyString(), any(), anyLong())).thenReturn(new StoredObject(100L, "checksum"));
		when(jobs.findById("job")).thenReturn(Optional.of(job));

		worker.processDueJobs();

		assertThat(job.getStatus()).isEqualTo(MediaProcessingJobStatus.SUCCEEDED);
		assertThat(secondJob.getStatus()).isEqualTo(MediaProcessingJobStatus.PENDING);
		verify(jobs, times(1)).findDueForUpdate(anyCollection(), any(), any());
	}

	private void stubDueJob() {
		when(jobs.findDueForUpdate(anyCollection(), any(), any())).thenReturn(List.of(job));
	}

	private byte[] jpeg(int width, int height) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "jpg", output);
			return output.toByteArray();
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
	}
}
