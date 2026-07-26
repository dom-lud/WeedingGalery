package pl.backend.weddinggallery.media.dto;

import jakarta.validation.constraints.Size;

public record MediaModerationRequest(@Size(max = 500) String reason) {
}
