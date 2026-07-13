package pl.backend.weddinggallery.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String name();

	HttpStatus getStatus();

	String getMessage();
}
