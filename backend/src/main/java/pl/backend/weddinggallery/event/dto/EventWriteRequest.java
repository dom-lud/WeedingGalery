package pl.backend.weddinggallery.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import pl.backend.weddinggallery.event.model.EventType;
import pl.backend.weddinggallery.event.model.PrivacyMode;

public record EventWriteRequest(@NotBlank @Size(max = 255) String name, @NotNull EventType type, LocalDate eventDate,
		@Size(max = 5000) String description, @NotNull PrivacyMode privacyMode) {
}
