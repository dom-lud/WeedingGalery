package pl.backend.weddinggallery.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.backend.weddinggallery.common.exception.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
		log.warn("Authentication failed: {}", ex.getMessage());
		return ResponseEntity.status(AuthErrorCode.INVALID_CREDENTIALS.getStatus()).body(ErrorResponse
				.of(AuthErrorCode.INVALID_CREDENTIALS.name(), AuthErrorCode.INVALID_CREDENTIALS.getMessage()));
	}
}
