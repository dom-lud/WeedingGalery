package pl.backend.weddinggallery.publicaccess.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record AccessCodeRequest(@NotBlank @Pattern(regexp = "[\\x20-\\x7E]{6,64}") String accessCode) {
}
