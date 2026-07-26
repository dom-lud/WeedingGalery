package pl.backend.weddinggallery.gallery.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum GalleryErrorCode implements ErrorCode {
	GALLERY_NOT_FOUND(HttpStatus.NOT_FOUND, "Gallery with the given ID was not found."), GALLERY_OWNER_REQUIRED(
			HttpStatus.FORBIDDEN,
			"Only the event owner can change gallery lifecycle."), GALLERY_ARCHIVED(HttpStatus.CONFLICT,
					"Archived gallery cannot be edited."), GALLERY_SLUG_CONFLICT(HttpStatus.CONFLICT,
							"A unique gallery slug could not be generated."), GALLERY_ACCESS_TOKEN_REQUIRED(
									HttpStatus.CONFLICT,
									"An access token is required before enabling public access."), GALLERY_INVALID_PUBLICATION_WINDOW(
											HttpStatus.BAD_REQUEST,
											"The publication window is invalid."), PUBLIC_GALLERY_NOT_FOUND(
													HttpStatus.NOT_FOUND,
													"Public gallery was not found."), GALLERY_ACCESS_CODE_REQUIRED(
															HttpStatus.UNAUTHORIZED,
															"An access code is required."), GALLERY_ACCESS_DENIED(
																	HttpStatus.UNAUTHORIZED,
																	"Gallery access was denied."), GALLERY_SETTINGS_OWNER_REQUIRED(
																			HttpStatus.FORBIDDEN,
																			"Only the event owner can change gallery settings."), GALLERY_CUSTOMIZATION_CONFLICT(
																					HttpStatus.CONFLICT,
																					"Gallery appearance changed elsewhere. Reload before saving.");

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
