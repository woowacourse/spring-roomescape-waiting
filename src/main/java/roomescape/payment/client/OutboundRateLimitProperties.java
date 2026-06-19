package roomescape.payment.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(
        @DefaultValue("10") int capacity,
        @DefaultValue("5.0") double refillPerSec
) {

    public OutboundRateLimitProperties {
        if (capacity <= 0) {
            throw new IllegalArgumentException("outbound-rate-limit.capacity must be positive");
        }
        if (refillPerSec <= 0) {
            throw new IllegalArgumentException("outbound-rate-limit.refill-per-sec must be positive");
        }
    }
}