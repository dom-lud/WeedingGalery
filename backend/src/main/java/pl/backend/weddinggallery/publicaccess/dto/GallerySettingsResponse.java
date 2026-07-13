package pl.backend.weddinggallery.publicaccess.dto;

import java.time.Instant;
import pl.backend.weddinggallery.gallery.model.ModerationMode;

public record GallerySettingsResponse(boolean publicViewEnabled, boolean uploadEnabled, boolean downloadEnabled,
		ModerationMode moderationMode, boolean accessTokenConfigured, boolean accessCodeConfigured, Instant publishedAt,
		Instant expiresAt, long version) {
}
