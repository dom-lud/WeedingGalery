package pl.backend.weddinggallery.upload.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum UploadErrorCode implements ErrorCode {
	UPLOAD_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Upload session was not found."), UPLOAD_SESSION_NOT_OPEN(
			HttpStatus.CONFLICT, "Upload session is not open."), UPLOAD_IN_PROGRESS(HttpStatus.CONFLICT,
					"The file is already being uploaded."), IDEMPOTENCY_KEY_CONFLICT(HttpStatus.CONFLICT,
							"Idempotency key was used with a different request."), UPLOAD_INVALID_IDEMPOTENCY_KEY(
									HttpStatus.BAD_REQUEST,
									"Idempotency key is invalid."), UPLOAD_FILE_NOT_FOUND(HttpStatus.NOT_FOUND,
											"Declared upload file was not found."), UPLOAD_FILE_TOO_LARGE(
													HttpStatus.PAYLOAD_TOO_LARGE,
													"The file exceeds its allowed size."), UPLOAD_TYPE_NOT_ALLOWED(
															HttpStatus.UNSUPPORTED_MEDIA_TYPE,
															"The file type is not allowed."), UPLOAD_CONTENT_MISMATCH(
																	HttpStatus.UNPROCESSABLE_ENTITY,
																	"File content does not match its declared type."), UPLOAD_SIZE_MISMATCH(
																			HttpStatus.UNPROCESSABLE_ENTITY,
																			"File size does not match the upload manifest."), UPLOAD_SESSION_LIMIT_EXCEEDED(
																					HttpStatus.CONFLICT,
																					"Too many active upload sessions."), STORAGE_QUOTA_EXCEEDED(
																							HttpStatus.CONFLICT,
																							"Gallery storage quota would be exceeded."), STORAGE_WRITE_FAILED(
																									HttpStatus.SERVICE_UNAVAILABLE,
																									"The file could not be stored safely.");

	private final HttpStatus status;
	private final String message;
	UploadErrorCode(HttpStatus status, String message) {
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
