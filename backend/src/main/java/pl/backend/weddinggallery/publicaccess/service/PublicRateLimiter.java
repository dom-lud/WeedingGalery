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
	private static final int MAX_KEYS = 10_000;
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
		check(key, limit);
	}

	public void check(String key, int operationLimit) {
		Instant now = clock.instant();
		if (!windows.containsKey(key) && windows.size() >= MAX_KEYS) {
			windows.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().resetAt()));
			if (windows.size() >= MAX_KEYS)
				windows.entrySet().stream()
						.min(java.util.Map.Entry.comparingByValue(java.util.Comparator.comparing(Window::resetAt)))
						.map(java.util.Map.Entry::getKey).ifPresent(windows::remove);
		}
		Window current = windows.compute(key,
				(ignored, previous) -> previous == null || !now.isBefore(previous.resetAt())
						? new Window(1, now.plus(window))
						: new Window(previous.count() + 1, previous.resetAt()));
		if (current.count() > operationLimit) {
			throw new RateLimitException(Math.max(1, Duration.between(now, current.resetAt()).toSeconds()));
		}
	}

	private record Window(int count, Instant resetAt) {
	}
}
