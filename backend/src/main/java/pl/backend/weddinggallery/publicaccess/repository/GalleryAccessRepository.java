package pl.backend.weddinggallery.publicaccess.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;

public interface GalleryAccessRepository extends JpaRepository<GalleryAccess, String> {
	List<GalleryAccess> findByGalleryIdAndRevokedAtIsNull(String galleryId);
	Optional<GalleryAccess> findByGalleryIdAndTokenHashAndRevokedAtIsNull(String galleryId, String tokenHash);
}
