package roomescape.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments")
public record PaymentProperties(
        Toss toss,
        long reservationAmount
) {
    public record Toss(
            String baseUrl,
            String clientKey,
            String secretKey
    ) {
    }
}
