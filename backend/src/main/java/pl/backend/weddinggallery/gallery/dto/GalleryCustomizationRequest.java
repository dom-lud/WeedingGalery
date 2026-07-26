package pl.backend.weddinggallery.gallery.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import pl.backend.weddinggallery.gallery.model.LayoutMode;
import pl.backend.weddinggallery.gallery.model.ThemeKey;

public record GalleryCustomizationRequest(@NotNull ThemeKey themeKey, @NotNull LayoutMode layoutMode,
		@NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String primaryColor,
		@NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String accentColor,
		@Size(max = 1000) @Pattern(regexp = "^[^<>]*$") String welcomeText,
		@Pattern(regexp = "^[0-9a-fA-F-]{36}$") String coverMediaId, @PositiveOrZero long version) {
}
