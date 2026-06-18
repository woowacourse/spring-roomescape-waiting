package roomescape.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(
        boolean enabled,
        long capacity,
        double refillPerSecond,
        int maxAttempts
) {

    public OutboundRateLimitProperties {
        if (capacity == 0) {
            capacity = 30;
        }
        if (refillPerSecond == 0D) {
            refillPerSecond = 30D;
        }
        if (maxAttempts == 0) {
            maxAttempts = 3;
        }
    }
}
