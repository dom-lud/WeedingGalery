package pl.backend.weddinggallery.publicaccess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicAccessRequest(@NotBlank @Size(max = 128) String accessToken, @Size(max = 64) String accessCode) {
}
