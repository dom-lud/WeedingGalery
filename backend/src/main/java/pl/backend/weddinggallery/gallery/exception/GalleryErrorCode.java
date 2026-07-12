package pl.backend.weddinggallery.gallery.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum GalleryErrorCode implements ErrorCode {
	GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "Gallery with the given ID was not found."), GALLERY_SLUG_ALREADY_EXISTS(
			HttpStatus.CONFLICT, "Gallery slug is already taken.");

	private final HttpStatus status;
	private final String message;

	GalleryErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
