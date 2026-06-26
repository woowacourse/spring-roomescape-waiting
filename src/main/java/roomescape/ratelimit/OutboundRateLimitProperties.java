package roomescape.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(long capacity, double refillPerSec) {
}
