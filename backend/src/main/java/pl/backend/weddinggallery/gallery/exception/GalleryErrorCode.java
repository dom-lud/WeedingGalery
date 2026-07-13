package pl.backend.weddinggallery.gallery.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum GalleryErrorCode implements ErrorCode {
	GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "Gallery with the given ID was not found."), GALLERY_OWNER_REQUIRED(
			HttpStatus.FORBIDDEN, "Only the event owner can change gallery lifecycle."), GALLERY_ARCHIVED(
					HttpStatus.CONFLICT, "Archived gallery cannot be edited."), GALLERY_SLUG_CONFLICT(
							HttpStatus.CONFLICT, "A unique gallery slug could not be generated.");

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
