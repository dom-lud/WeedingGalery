package pl.backend.weddinggallery.common.exception;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ErrorResponse(String code, String message, List<ErrorDetail> details, String correlationId,
		Instant timestamp) {
	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(code, message, List.of(), UUID.randomUUID().toString(), Instant.now());
	}

	public static ErrorResponse validation(List<ErrorDetail> details) {
		return new ErrorResponse("VALIDATION_ERROR", "Request validation failed.", details,
				UUID.randomUUID().toString(), Instant.now());
	}

	public record ErrorDetail(String field, String code) {
	}
}
