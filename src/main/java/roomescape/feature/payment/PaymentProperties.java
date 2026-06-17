package roomescape.feature.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentProperties {

    private final String clientKey;
    private final long amount;

    public PaymentProperties(
            @Value("${toss.payments.client-key}") String clientKey,
            @Value("${toss.payments.amount}") long amount
    ) {
        this.clientKey = clientKey;
        this.amount = amount;
    }

    public String clientKey() {
        return clientKey;
    }

    public long amount() {
        return amount;
    }
}
