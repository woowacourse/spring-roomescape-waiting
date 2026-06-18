package roomescape.global.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.retry")
public record TossRetryProperties(int maxAttempts, long fallbackWaitSeconds) {
}
