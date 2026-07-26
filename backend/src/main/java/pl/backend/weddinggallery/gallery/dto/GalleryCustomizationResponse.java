package pl.backend.weddinggallery.gallery.dto;

public record GalleryCustomizationResponse(String theme, String layout, String primaryColor, String accentColor,
		String backgroundColor, String welcomeText, boolean showTitle, boolean showUpload, boolean showDownload,
		String coverMediaId, long version) {
}
