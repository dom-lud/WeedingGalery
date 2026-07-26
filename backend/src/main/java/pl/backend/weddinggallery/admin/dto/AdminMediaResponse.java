package pl.backend.weddinggallery.admin.dto;

import java.time.LocalDateTime;
import pl.backend.weddinggallery.media.model.MediaStatus;
import pl.backend.weddinggallery.media.model.MediaType;
import pl.backend.weddinggallery.media.model.PublicationStatus;

public record AdminMediaResponse(String id, String galleryId, String eventId, String fileName, MediaType mediaType,
		MediaStatus status, PublicationStatus publicationStatus, Long sizeBytes, LocalDateTime storedAt) {
}
