package pl.backend.weddinggallery.publicaccess.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.backend.weddinggallery.common.exception.RateLimitException;

@Component
public class PublicRateLimiter {
	private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
	private final Clock clock = Clock.systemUTC();
	private final int limit;
	private final Duration window;

	public PublicRateLimiter(@Value("${app.public-access.rate-limit:10}") int limit,
			@Value("${app.public-access.rate-window-seconds:900}") long windowSeconds) {
		this.limit = limit;
		this.window = Duration.ofSeconds(windowSeconds);
	}

	public void check(String key) {
		Instant now = clock.instant();
		Window current = windows.compute(key,
				(ignored, previous) -> previous == null || !now.isBefore(previous.resetAt())
						? new Window(1, now.plus(window))
						: new Window(previous.count() + 1, previous.resetAt()));
		if (current.count() > limit) {
			throw new RateLimitException(Math.max(1, Duration.between(now, current.resetAt()).toSeconds()));
		}
		if (windows.size() > 10_000) {
			windows.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().resetAt()));
		}
	}

	private record Window(int count, Instant resetAt) {
	}
}
