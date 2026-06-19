package roomescape.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentCheckoutProperties {

    private static final String PLACEHOLDER_CLIENT_KEY = "test_ck_placeholder";

    private final String clientKey;

    public PaymentCheckoutProperties(@Value("${payment.client-key:test_ck_placeholder}") String clientKey) {
        this.clientKey = clientKey;
    }

    public String getClientKey() {
        return clientKey;
    }

    public boolean isConfigured() {
        return clientKey != null
                && !clientKey.isBlank()
                && !PLACEHOLDER_CLIENT_KEY.equals(clientKey)
                && clientKey.startsWith("test_ck_");
    }
}
