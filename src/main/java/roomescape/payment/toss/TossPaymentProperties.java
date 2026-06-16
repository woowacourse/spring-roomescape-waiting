package roomescape.payment.toss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TossPaymentProperties {

    private final String secretKey;
    private final String confirmUrl;

    @Autowired
    public TossPaymentProperties(
            Environment environment,
            @Value("${payment.toss.confirm-url:https://api.tosspayments.com/v1/payments/confirm}") String confirmUrl
    ) {
        this.secretKey = firstConfigured(
                environment.getProperty("payment.toss.secret-key"),
                environment.getProperty("toss.secret-key"),
                environment.getProperty("TOSS_SECRET_KEY"),
                environment.getProperty("TOSS_PAYMENTS_SECRET_KEY"),
                System.getenv("TOSS_SECRET_KEY"),
                System.getenv("TOSS_PAYMENTS_SECRET_KEY")
        );
        this.confirmUrl = confirmUrl;
    }

    public TossPaymentProperties(String secretKey, String confirmUrl) {
        this.secretKey = secretKey;
        this.confirmUrl = confirmUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getConfirmUrl() {
        return confirmUrl;
    }

    private String firstConfigured(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return "";
    }
}
