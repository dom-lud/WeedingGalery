package pl.backend.weddinggallery.upload.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.upload.model.UploadSession;
import pl.backend.weddinggallery.upload.model.UploadSessionStatus;

public interface UploadSessionRepository extends JpaRepository<UploadSession, String> {
	Optional<UploadSession> findByPublicAccessIdAndIdempotencyKey(String accessId, String idempotencyKey);
	Optional<UploadSession> findByIdAndGalleryIdAndPublicAccessIdAndGrantFingerprint(String id, String galleryId,
			String accessId, String grantFingerprint);
	long countByPublicAccessIdAndStatusAndExpiresAtAfter(String accessId, UploadSessionStatus status,
			LocalDateTime now);
}
