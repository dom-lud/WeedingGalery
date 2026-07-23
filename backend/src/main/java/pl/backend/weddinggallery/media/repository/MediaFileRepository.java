package pl.backend.weddinggallery.media.repository;

import java.util.*;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.media.model.MediaStatus;
import pl.backend.weddinggallery.media.model.MediaFile;

public interface MediaFileRepository extends JpaRepository<MediaFile, String> {
	Optional<MediaFile> findByUploadSessionIdAndClientFileId(String sessionId, String clientFileId);
	Optional<MediaFile> findByIdAndGalleryId(String id, String galleryId);
	List<MediaFile> findByStatusAndUpdatedAtBefore(MediaStatus status, LocalDateTime cutoff);
	List<MediaFile> findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(String galleryId,
			Collection<MediaStatus> statuses);
}
