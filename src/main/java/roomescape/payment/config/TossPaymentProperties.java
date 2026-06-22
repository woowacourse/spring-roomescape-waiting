package roomescape.payment.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
        String baseUrl,
        String secretKey,
        String clientKey,
        Duration connectTimeout,
        Duration readTimeout
) {
}
