package pl.backend.weddinggallery.event.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum EventErrorCode implements ErrorCode {
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Event was not found."), EVENT_OWNER_REQUIRED(HttpStatus.FORBIDDEN,
			"Only the event owner can perform this operation."), EVENT_ARCHIVED(HttpStatus.CONFLICT,
					"Archived event cannot be modified.");

	private final HttpStatus status;
	private final String message;

	EventErrorCode(HttpStatus status, String message) {
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
