package pl.backend.weddinggallery.publicaccess.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	void resetsExpiredWindowsAndUsesAtLeastOneSecondRetryForDegenerateConfiguration() {
		PublicRateLimiter immediatelyResetting = new PublicRateLimiter(1, 0);
		assertThatCode(() -> {
			immediatelyResetting.check("same");
			immediatelyResetting.check("same");
		}).doesNotThrowAnyException();

		PublicRateLimiter zeroLimit = new PublicRateLimiter(0, 0);
		assertThatThrownBy(() -> zeroLimit.check("blocked")).isInstanceOfSatisfying(RateLimitException.class,
				error -> assertThat(((RateLimitException) error).getRetryAfterSeconds()).isEqualTo(1));
	}

	@Test
	void boundsAttackerControlledBucketCardinalityForActiveAndExpiredWindows() {
		PublicRateLimiter active = new PublicRateLimiter(Integer.MAX_VALUE, 900);
		for (int index = 0; index <= 10_000; index++)
			active.check("active-" + index);
		assertThatCode(() -> active.check("active-new")).doesNotThrowAnyException();

		PublicRateLimiter expired = new PublicRateLimiter(Integer.MAX_VALUE, 0);
		for (int index = 0; index <= 10_000; index++)
			expired.check("expired-" + index);
		assertThatCode(() -> expired.check("expired-new")).doesNotThrowAnyException();
	}
}
