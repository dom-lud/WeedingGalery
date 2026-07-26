package pl.backend.weddinggallery.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class GlobalExceptionHandlerTest {
	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void mapsRateLimitAndUploadSize() {
		var rate = handler.handleRateLimit(new RateLimitException(17));
		assertThat(rate.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(rate.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("17");
		assertThat(rate.getBody().code()).isEqualTo("RATE_LIMIT_EXCEEDED");

		var size = handler.handleMaxUploadSize(mock(MaxUploadSizeExceededException.class));
		assertThat(size.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
		assertThat(size.getBody().code()).isEqualTo("UPLOAD_FILE_TOO_LARGE");
	}

	@Test
	void mapsApplicationUnreadableConflictAndUnexpectedErrors() {
		var app = handler.handleAppException(new AppException(new ErrorCode() {
			@Override
			public HttpStatus getStatus() {
				return HttpStatus.FORBIDDEN;
			}
			@Override
			public String getMessage() {
				return "Rejected";
			}
			@Override
			public String name() {
				return "REJECTED";
			}
		}));
		assertThat(app.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(app.getBody().code()).isEqualTo("REJECTED");

		var unreadable = handler.handleUnreadableMessage(
				new HttpMessageNotReadableException("bad", new MockHttpInputMessage(new byte[0])));
		assertThat(unreadable.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(handler.handleConflict(new DataIntegrityViolationException("duplicate")).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);
		assertThat(handler.handleConflict(mock(ObjectOptimisticLockingFailureException.class)).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);
		assertThat(handler.handleGeneralException(new IllegalStateException()).getStatusCode())
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	void mapsValidationCodesAndInvalidFallback() {
		BindingResult binding = mock(BindingResult.class);
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(binding);
		when(binding.getFieldErrors()).thenReturn(
				List.of(new FieldError("request", "name", null, false, new String[]{"required"}, null, "missing"),
						new FieldError("request", "email", null, false, null, null, "invalid")));

		var response = handler.handleValidationException(exception);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().details()).extracting(ErrorResponse.ErrorDetail::field).containsExactly("name",
				"email");
		assertThat(response.getBody().details()).extracting(ErrorResponse.ErrorDetail::code).containsExactly("REQUIRED",
				"INVALID");
	}
}
