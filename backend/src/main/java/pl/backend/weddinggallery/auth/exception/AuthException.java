package pl.backend.weddinggallery.auth.exception;

import pl.backend.weddinggallery.common.exception.AppException;

public class AuthException extends AppException {
	public AuthException(AuthErrorCode errorCode) {
		super(errorCode);
	}
}
