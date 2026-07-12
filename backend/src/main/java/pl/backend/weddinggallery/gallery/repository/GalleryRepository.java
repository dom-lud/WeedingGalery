package pl.backend.weddinggallery.gallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.gallery.model.Gallery;

import java.util.List;
import java.util.Optional;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, String> {
	Optional<Gallery> findBySlug(String slug);
	List<Gallery> findByEventId(String eventId);
}
