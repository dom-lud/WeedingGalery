package pl.backend.weddinggallery.media.service;

class MediaProcessingException extends RuntimeException {
	private final String code;
	private final boolean retryable;

	MediaProcessingException(String code, String message, boolean retryable) {
		super(message);
		this.code = code;
		this.retryable = retryable;
	}

	String code() {
		return code;
	}

	boolean retryable() {
		return retryable;
	}
}
