package pl.backend.weddinggallery.media.repository;

import java.util.*;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.media.model.MediaFile;

public interface MediaFileRepository extends JpaRepository<MediaFile, String> {
	Optional<MediaFile> findByUploadSessionIdAndClientFileId(String sessionId, String clientFileId);
	List<MediaFile> findByStatusAndUpdatedAtBefore(pl.backend.weddinggallery.media.model.MediaStatus status,
			LocalDateTime cutoff);
}
