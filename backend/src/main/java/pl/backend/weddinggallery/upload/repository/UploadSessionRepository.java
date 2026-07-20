package pl.backend.weddinggallery.upload.repository;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.backend.weddinggallery.upload.model.UploadSession;
import pl.backend.weddinggallery.upload.model.UploadSessionStatus;

public interface UploadSessionRepository extends JpaRepository<UploadSession, String> {
	Optional<UploadSession> findByGrantFingerprintAndIdempotencyKey(String grantFingerprint, String idempotencyKey);
	Optional<UploadSession> findByIdAndGalleryIdAndPublicAccessIdAndGrantFingerprint(String id, String galleryId,
			String accessId, String grantFingerprint);
	long countByGrantFingerprintAndStatusAndExpiresAtAfter(String grantFingerprint, UploadSessionStatus status,
			LocalDateTime now);
	@Query("select distinct s from UploadSession s left join fetch s.files where s.gallery.id = :galleryId and s.status = 'OPEN' and (s.expiresAt <= :now or s.publicAccess.revokedAt is not null)")
	List<UploadSession> findReleasableOpenSessions(String galleryId, LocalDateTime now);
	@Query("select distinct s from UploadSession s left join fetch s.files where s.status = 'OPEN' and (s.expiresAt <= :now or s.publicAccess.revokedAt is not null)")
	List<UploadSession> findAllReleasableOpenSessions(LocalDateTime now);
}
