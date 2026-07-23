package pl.backend.weddinggallery.publicaccess.dto;

import java.time.Instant;
import java.util.List;
import pl.backend.weddinggallery.gallery.model.ModerationMode;
import pl.backend.weddinggallery.media.dto.MediaItemResponse;

public record PublicGalleryResponse(String slug, String name, String description, boolean uploadEnabled,
		boolean downloadEnabled, ModerationMode moderationMode, Instant publishedAt, Instant expiresAt,
		List<MediaItemResponse> media) {
}
