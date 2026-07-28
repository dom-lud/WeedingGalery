package pl.backend.weddinggallery.qr.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum QrErrorCode implements ErrorCode {
	QR_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "QR format is not supported."), QR_INVALID_SIZE(HttpStatus.BAD_REQUEST,
			"QR size must be between 128 and 2048 pixels."), QR_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
					"QR code could not be generated.");

	private final HttpStatus status;
	private final String message;

	QrErrorCode(HttpStatus status, String message) {
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
