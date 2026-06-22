package roomescape.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(
        Integer capacity,
        Integer refillPerSec
) {

    private static final int DEFAULT_CAPACITY = 1_000;
    private static final int DEFAULT_REFILL_PER_SEC = 1_000;

    public OutboundRateLimitProperties {
        if (capacity == null) {
            capacity = DEFAULT_CAPACITY;
        }
        if (refillPerSec == null) {
            refillPerSec = DEFAULT_REFILL_PER_SEC;
        }
    }
}
