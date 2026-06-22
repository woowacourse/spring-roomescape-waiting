package roomescape.payment.infrastructure.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
        String baseUrl,
        String clientKey,
        String secretKey,
        Duration connectTimeout,
        Duration readTimeout
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
    }
}
