package roomescape.ratelimit;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetryAfterProperties {

    private final int maxAttempts;
    private final Duration fallbackDelay;

    public RetryAfterProperties(
            @Value("${payment.toss.retry.max-attempts:3}") int maxAttempts,
            @Value("${payment.toss.retry.fallback-delay:PT1S}") Duration fallbackDelay
    ) {
        this.maxAttempts = maxAttempts;
        this.fallbackDelay = fallbackDelay;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getFallbackDelay() {
        return fallbackDelay;
    }
}
