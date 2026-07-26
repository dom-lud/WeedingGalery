package pl.backend.weddinggallery.media.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum MediaModerationErrorCode implements ErrorCode {
	MEDIA_MODERATION_NOT_FOUND(HttpStatus.NOT_FOUND,
			"Media was not found in the requested gallery."), MEDIA_MODERATION_BULK_LIMIT_EXCEEDED(
					HttpStatus.BAD_REQUEST,
					"Bulk moderation is limited to 100 media items."), MEDIA_MODERATION_EMPTY_BULK(
							HttpStatus.BAD_REQUEST,
							"At least one media item is required."), MEDIA_MODERATION_DUPLICATE_MEDIA(
									HttpStatus.BAD_REQUEST,
									"A media item may occur only once in a bulk action."), MEDIA_MODERATION_INVALID_TRANSITION(
											HttpStatus.CONFLICT,
											"The media publication status cannot be changed by this action."), MEDIA_MODERATION_REASON_REQUIRED(
													HttpStatus.BAD_REQUEST,
													"A reason is required for this moderation action.");

	private final HttpStatus status;
	private final String message;

	MediaModerationErrorCode(HttpStatus status, String message) {
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
