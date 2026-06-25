package roomescape.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentProperties(String secretKey, String clientKey, String baseUrl,
                                    int connectTimeoutMs, int readTimeoutMs, int maxRetryAttempts) {
}
