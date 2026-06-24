package roomescape.payment.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss")
public record TossProperties(
        String baseUrl,
        String clientKey,
        String secretKey
) {
}
