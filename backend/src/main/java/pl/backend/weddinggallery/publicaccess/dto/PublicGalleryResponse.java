package pl.backend.weddinggallery.publicaccess.dto;

import java.time.Instant;
import pl.backend.weddinggallery.gallery.model.ModerationMode;

public record PublicGalleryResponse(String slug, String name, String description, boolean uploadEnabled,
		boolean downloadEnabled, ModerationMode moderationMode, Instant publishedAt, Instant expiresAt) {
}
