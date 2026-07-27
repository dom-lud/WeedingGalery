package pl.backend.weddinggallery.qr.model;

public enum QrFormat {
	PNG("image/png"), SVG("image/svg+xml");

	private final String mediaType;

	QrFormat(String mediaType) {
		this.mediaType = mediaType;
	}

	public String mediaType() {
		return mediaType;
	}
}
