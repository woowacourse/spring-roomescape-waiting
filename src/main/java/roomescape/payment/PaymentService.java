package roomescape.payment;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult confirm(String paymentKey, String orderId, String idempotencyKey, Long amount) {
        return paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, idempotencyKey, amount));
    }
}
