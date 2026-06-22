package roomescape.ratelimit;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(
        long capacity,
        long refillPerSecond,
        int maxAttempts,
        Duration fallbackRetryAfter
) {
}
