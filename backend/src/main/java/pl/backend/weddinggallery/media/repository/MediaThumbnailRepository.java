package pl.backend.weddinggallery.media.repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.media.model.*;

public interface MediaThumbnailRepository extends JpaRepository<MediaThumbnail, String> {
	Optional<MediaThumbnail> findByMediaFileIdAndVariant(String mediaFileId, MediaThumbnailVariant variant);
	List<MediaThumbnail> findByMediaFileIdInAndVariant(Collection<String> mediaFileIds, MediaThumbnailVariant variant);
}
