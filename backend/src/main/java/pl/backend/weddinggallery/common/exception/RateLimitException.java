package pl.backend.weddinggallery.common.exception;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {
	private final long retryAfterSeconds;
	public RateLimitException(long retryAfterSeconds) {
		super("Too many requests.");
		this.retryAfterSeconds = retryAfterSeconds;
	}
}
