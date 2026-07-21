package pl.backend.weddinggallery.media.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pl.backend.weddinggallery.media.model.*;

public interface MediaProcessingJobRepository extends JpaRepository<MediaProcessingJob, String> {
	Optional<MediaProcessingJob> findByMediaFileIdAndJobType(String mediaFileId, MediaProcessingJobType jobType);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select job from MediaProcessingJob job
			join fetch job.mediaFile media
			where job.status in :statuses and job.scheduledAt <= :now
			order by job.scheduledAt asc, job.createdAt asc, job.id asc
			""")
	List<MediaProcessingJob> findDueForUpdate(@Param("statuses") Collection<MediaProcessingJobStatus> statuses,
			@Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);
}
