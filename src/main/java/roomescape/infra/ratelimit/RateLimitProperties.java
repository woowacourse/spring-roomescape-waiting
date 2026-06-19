package roomescape.infra.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        @DefaultValue("20") int capacity,
        @DefaultValue("10.0") double refillPerSec
) {

    public RateLimitProperties {
        if (capacity <= 0) {
            throw new IllegalArgumentException("rate-limit.capacity must be positive");
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("rate-limit.refill-per-sec must be positive");
        }
    }
}