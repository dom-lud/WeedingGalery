package pl.backend.weddinggallery.publicaccess.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import pl.backend.weddinggallery.gallery.model.ModerationMode;

public record GallerySettingsRequest(boolean publicViewEnabled, boolean uploadEnabled, boolean downloadEnabled,
		@NotNull ModerationMode moderationMode, Instant publishedAt, Instant expiresAt, @PositiveOrZero long version) {
}
