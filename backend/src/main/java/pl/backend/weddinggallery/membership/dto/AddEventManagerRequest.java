package pl.backend.weddinggallery.membership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.backend.weddinggallery.membership.model.EventRole;

public record AddEventManagerRequest(@Email @NotBlank String email, @NotNull EventRole role) {
}
