package roomescape.payment.toss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossPaymentProperties {

    private final String secretKey;
    private final String confirmUrl;

    public TossPaymentProperties(
            @Value("${payment.toss.secret-key:}") String secretKey,
            @Value("${payment.toss.confirm-url:https://api.tosspayments.com/v1/payments/confirm}") String confirmUrl
    ) {
        this.secretKey = secretKey;
        this.confirmUrl = confirmUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getConfirmUrl() {
        return confirmUrl;
    }
}
