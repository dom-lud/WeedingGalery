package pl.backend.weddinggallery.publicaccess.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pl.backend.weddinggallery.common.exception.RateLimitException;

class PublicRateLimiterTest {

	@Test
	void rejectsOnlyRequestsBeyondTheConfiguredOperationLimit() {
		PublicRateLimiter limiter = new PublicRateLimiter(10, 900);

		assertThatCode(() -> {
			limiter.check("ip:access:gallery", 2);
			limiter.check("ip:access:gallery", 2);
		}).doesNotThrowAnyException();

		assertThatThrownBy(() -> limiter.check("ip:access:gallery", 2)).isInstanceOf(RateLimitException.class)
				.satisfies(error -> org.assertj.core.api.Assertions
						.assertThat(((RateLimitException) error).getRetryAfterSeconds()).isPositive());
	}

	@Test
	void keepsRateLimitBucketsIsolatedByTheCompleteSecurityKey() {
		PublicRateLimiter limiter = new PublicRateLimiter(1, 900);

		limiter.check("ip-a:access:gallery-a");

		assertThatCode(() -> {
			limiter.check("ip-a:access:gallery-b");
			limiter.check("ip-b:access:gallery-a");
		}).doesNotThrowAnyException();
		assertThatThrownBy(() -> limiter.check("ip-a:access:gallery-a")).isInstanceOf(RateLimitException.class);
	}
}
