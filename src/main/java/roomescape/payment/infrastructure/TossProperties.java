package roomescape.payment.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss")
public record TossProperties(
        String baseUrl,
        String secretKey,
        String clientKey,
        Duration connectTimeout,
        Duration readTimeout
) {
}
