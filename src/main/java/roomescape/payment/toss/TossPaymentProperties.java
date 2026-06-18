package roomescape.payment.toss;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossPaymentProperties {

    private final String secretKey;
    private final String confirmUrl;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    public TossPaymentProperties(
            @Value("${payment.toss.secret-key:}") String secretKey,
            @Value("${payment.toss.confirm-url:https://api.tosspayments.com/v1/payments/confirm}") String confirmUrl,
            @Value("${payment.toss.connect-timeout:PT2S}") Duration connectTimeout,
            @Value("${payment.toss.read-timeout:PT5S}") Duration readTimeout
    ) {
        this.secretKey = secretKey;
        this.confirmUrl = confirmUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getConfirmUrl() {
        return confirmUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }
}
