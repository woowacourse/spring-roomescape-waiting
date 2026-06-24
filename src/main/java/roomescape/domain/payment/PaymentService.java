package roomescape.domain.payment;

import org.springframework.stereotype.Service;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

@Service
public class PaymentService {

    private final PaymentClient paymentClient;

    public PaymentService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        return paymentClient.confirm(request);
    }
}
