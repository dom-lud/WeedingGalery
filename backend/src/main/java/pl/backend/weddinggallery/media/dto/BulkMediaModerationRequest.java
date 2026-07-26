package pl.backend.weddinggallery.media.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BulkMediaModerationRequest(@NotEmpty @Size(max = 100) List<@NotEmpty String> mediaIds,
		@NotNull MediaModerationAction action, @Size(max = 500) String reason) {
	public BulkMediaModerationRequest(List<@NotEmpty String> mediaIds, MediaModerationAction action) {
		this(mediaIds, action, null);
	}
}
