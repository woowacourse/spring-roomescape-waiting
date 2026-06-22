package roomescape.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(long capacity, double refillPerSec) {

    public RateLimitProperties {
        validate(capacity, refillPerSec);
    }

    private static void validate(long capacity, double refillPerSec) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("rate-limit.capacity는 0보다 커야 합니다.");
        }
        if (!Double.isFinite(refillPerSec) || refillPerSec <= 0) {
            throw new IllegalArgumentException("rate-limit.refill-per-sec는 0보다 큰 유한한 값이어야 합니다.");
        }
    }
}
