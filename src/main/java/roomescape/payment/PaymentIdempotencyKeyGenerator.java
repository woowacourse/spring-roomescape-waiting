package roomescape.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class PaymentIdempotencyKeyGenerator {

    public String generate() {
        return UUID.randomUUID().toString();
    }
}
