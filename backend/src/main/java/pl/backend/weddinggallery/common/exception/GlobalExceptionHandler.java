package pl.backend.weddinggallery.common.exception;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
		log.warn("AppException thrown: {} - {}", ex.getErrorCode().name(), ex.getMessage());
		return ResponseEntity.status(ex.getErrorCode().getStatus())
				.body(ErrorResponse.of(ex.getErrorCode().name(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		List<ErrorResponse.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new ErrorResponse.ErrorDetail(error.getField(), validationCode(error))).toList();
		log.warn("Validation failed for fields: {}", details.stream().map(ErrorResponse.ErrorDetail::field).toList());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.validation(details));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
		log.warn("Unreadable request payload");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of("VALIDATION_ERROR", "Request payload is invalid."));
	}

	@ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
	public ResponseEntity<ErrorResponse> handleConflict(Exception ex) {
		log.warn("Concurrent or integrity conflict: {}", ex.getClass().getSimpleName());
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of("CONCURRENT_MODIFICATION", "Resource was modified concurrently."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		log.error("Unhandled exception occurred", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred."));
	}

	private String validationCode(FieldError error) {
		return error.getCode() == null ? "INVALID" : error.getCode().toUpperCase();
	}
}
