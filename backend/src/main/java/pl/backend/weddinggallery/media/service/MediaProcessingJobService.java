package pl.backend.weddinggallery.media.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.media.repository.MediaProcessingJobRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingJobService {
	private final MediaProcessingJobRepository jobs;
	@Value("${app.media.processing.max-attempts:3}")
	private int maxAttempts;

	@Transactional
	public MediaProcessingJob enqueue(MediaFile mediaFile) {
		return jobs.findByMediaFileIdAndJobType(mediaFile.getId(), MediaProcessingJobType.PROCESS_MEDIA)
				.orElseGet(() -> {
					MediaProcessingJob job = jobs.save(MediaProcessingJob.builder().mediaFile(mediaFile)
							.jobType(MediaProcessingJobType.PROCESS_MEDIA).status(MediaProcessingJobStatus.PENDING)
							.maxAttempts(maxAttempts).scheduledAt(LocalDateTime.now()).build());
					log.info("Media processing job enqueued jobId={} mediaId={} galleryId={}", job.getId(),
							mediaFile.getId(), mediaFile.getGallery().getId());
					return job;
				});
	}
}
