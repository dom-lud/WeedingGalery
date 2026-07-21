package pl.backend.weddinggallery.media.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MediaProcessingModelTest {
	@Test
	void mediaProcessingJobPrePersistSetsOnlyMissingDefaults() {
		MediaProcessingJob defaults = new MediaProcessingJob();
		defaults.create();

		assertThat(defaults.getId()).isNotBlank();
		assertThat(defaults.getStatus()).isEqualTo(MediaProcessingJobStatus.PENDING);
		assertThat(defaults.getJobType()).isEqualTo(MediaProcessingJobType.PROCESS_MEDIA);
		assertThat(defaults.getMaxAttempts()).isEqualTo(3);
		assertThat(defaults.getScheduledAt()).isNotNull();
		assertThat(defaults.getCreatedAt()).isEqualTo(defaults.getUpdatedAt());

		LocalDateTime scheduled = LocalDateTime.now().plusDays(1);
		MediaProcessingJob explicit = MediaProcessingJob.builder().id("job").status(MediaProcessingJobStatus.RUNNING)
				.jobType(MediaProcessingJobType.PROCESS_MEDIA).maxAttempts(5).scheduledAt(scheduled).build();
		explicit.create();

		assertThat(explicit.getId()).isEqualTo("job");
		assertThat(explicit.getStatus()).isEqualTo(MediaProcessingJobStatus.RUNNING);
		assertThat(explicit.getMaxAttempts()).isEqualTo(5);
		assertThat(explicit.getScheduledAt()).isEqualTo(scheduled);
	}

	@Test
	void mediaProcessingJobPreUpdateRefreshesTimestamp() {
		MediaProcessingJob job = new MediaProcessingJob();
		job.create();
		LocalDateTime created = job.getUpdatedAt();

		job.update();

		assertThat(job.getUpdatedAt()).isAfterOrEqualTo(created);
	}

	@Test
	void mediaThumbnailPrePersistSetsOnlyMissingDefaults() {
		MediaThumbnail defaults = new MediaThumbnail();
		defaults.create();

		assertThat(defaults.getId()).isNotBlank();
		assertThat(defaults.getVariant()).isEqualTo(MediaThumbnailVariant.SMALL);
		assertThat(defaults.getCreatedAt()).isNotNull();

		MediaThumbnail explicit = MediaThumbnail.builder().id("thumbnail").variant(MediaThumbnailVariant.SMALL).build();
		explicit.create();

		assertThat(explicit.getId()).isEqualTo("thumbnail");
		assertThat(explicit.getVariant()).isEqualTo(MediaThumbnailVariant.SMALL);
	}
}
