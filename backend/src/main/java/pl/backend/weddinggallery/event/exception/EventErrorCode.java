package pl.backend.weddinggallery.event.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum EventErrorCode implements ErrorCode {
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Event with the given ID was not found."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "You do not have permission to access this event.");

    private final HttpStatus status;
    private final String message;

    EventErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() { return status; }

    @Override
    public String getMessage() { return message; }
}
