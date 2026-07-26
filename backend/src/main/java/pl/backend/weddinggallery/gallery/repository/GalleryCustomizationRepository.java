package pl.backend.weddinggallery.gallery.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.gallery.model.GalleryCustomization;

public interface GalleryCustomizationRepository extends JpaRepository<GalleryCustomization, String> {
	Optional<GalleryCustomization> findByGalleryId(String galleryId);
}
