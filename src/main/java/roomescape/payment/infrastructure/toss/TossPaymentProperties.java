package roomescape.payment.infrastructure.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
        String baseUrl,
        String clientKey,
        String secretKey,
        Duration connectTimeout,
        Duration readTimeout,
        RateLimitRetry rateLimitRetry
) {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(5);

    public TossPaymentProperties {
        if (connectTimeout == null) {
            connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        }
        if (readTimeout == null) {
            readTimeout = DEFAULT_READ_TIMEOUT;
        }
        if (rateLimitRetry == null) {
            rateLimitRetry = RateLimitRetry.defaults();
        }
    }

    public record RateLimitRetry(
            Integer maxAttempts,
            Duration fallbackBackoff
    ) {

        private static final int DEFAULT_MAX_ATTEMPTS = 3;
        private static final Duration DEFAULT_FALLBACK_BACKOFF = Duration.ofSeconds(1);

        public RateLimitRetry {
            if (maxAttempts == null) {
                maxAttempts = DEFAULT_MAX_ATTEMPTS;
            }
            if (fallbackBackoff == null) {
                fallbackBackoff = DEFAULT_FALLBACK_BACKOFF;
            }
        }

        private static RateLimitRetry defaults() {
            return new RateLimitRetry(null, null);
        }
    }
}
