package roomescape.adapter.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss")
public record TossProperties(
        String baseUrl,
        String secretKey,
        String clientKey,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
