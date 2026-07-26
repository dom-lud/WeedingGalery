package pl.backend.weddinggallery.gallery.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
public record GalleryCustomizationRequest(@NotNull @Pattern(regexp = "EDITORIAL|MINIMAL|ROMANTIC") String theme,
		@NotNull @Pattern(regexp = "GRID|MASONRY|TIMELINE") String layout,
		@NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String primaryColor,
		@NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String accentColor,
		@NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}") String backgroundColor,
		@Size(max = 500) @Pattern(regexp = "^[^<>]*$") String welcomeText, @NotNull Boolean showTitle,
		@NotNull Boolean showUpload, @NotNull Boolean showDownload,
		@Pattern(regexp = "^[0-9a-fA-F-]{36}$") String coverMediaId, @PositiveOrZero long version) {
}
