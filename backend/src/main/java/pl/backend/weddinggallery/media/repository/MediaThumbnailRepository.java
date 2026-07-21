package pl.backend.weddinggallery.media.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.media.model.*;

public interface MediaThumbnailRepository extends JpaRepository<MediaThumbnail, String> {
	Optional<MediaThumbnail> findByMediaFileIdAndVariant(String mediaFileId, MediaThumbnailVariant variant);
}
