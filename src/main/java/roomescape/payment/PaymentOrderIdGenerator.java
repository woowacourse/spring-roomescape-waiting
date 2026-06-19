package roomescape.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class PaymentOrderIdGenerator {

    public String generate() {
        return "order-" + UUID.randomUUID();
    }
}
