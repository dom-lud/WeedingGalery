package pl.backend.weddinggallery.media.dto;

import java.time.Instant;
import pl.backend.weddinggallery.media.model.MediaStatus;
import pl.backend.weddinggallery.media.model.MediaType;

public record MediaItemResponse(String id, String fileName, MediaType mediaType, MediaStatus status, Long size,
		Instant uploadedAt, String thumbnailUrl, String contentUrl) {
}
