package roomescape.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(long capacity, double refillPerSec) {

    public OutboundRateLimitProperties {
        if (capacity <= 0) {
            throw new IllegalArgumentException("outbound-rate-limit.capacity는 0보다 커야 합니다.");
        }
        if (!Double.isFinite(refillPerSec) || refillPerSec <= 0) {
            throw new IllegalArgumentException(
                    "outbound-rate-limit.refill-per-sec는 0보다 큰 유한한 값이어야 합니다."
            );
        }
    }
}
