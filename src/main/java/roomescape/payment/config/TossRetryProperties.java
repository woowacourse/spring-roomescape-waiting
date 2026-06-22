package roomescape.payment.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments.toss.retry")
public record TossRetryProperties(int maxAttempts, Duration fallbackDelay) {

    public TossRetryProperties {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts는 0보다 커야 합니다.");
        }
        if (fallbackDelay == null || fallbackDelay.isNegative()) {
            throw new IllegalArgumentException("fallbackDelay는 0 이상이어야 합니다.");
        }
    }
}
