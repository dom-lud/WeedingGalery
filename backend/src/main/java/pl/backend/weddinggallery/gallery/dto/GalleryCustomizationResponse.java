package pl.backend.weddinggallery.gallery.dto;

import pl.backend.weddinggallery.gallery.model.LayoutMode;
import pl.backend.weddinggallery.gallery.model.ThemeKey;

public record GalleryCustomizationResponse(ThemeKey themeKey, LayoutMode layoutMode, String primaryColor,
		String accentColor, String welcomeText, String coverMediaId, long version) {
}
