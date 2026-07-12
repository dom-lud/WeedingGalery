package pl.backend.weddinggallery.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

@Getter
public enum AuthErrorCode implements ErrorCode {
	EMAIL_ALREADY_IN_USE(HttpStatus.BAD_REQUEST, "Email already in use"), INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,
			"Invalid email or password");

	private final HttpStatus status;
	private final String message;

	AuthErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
