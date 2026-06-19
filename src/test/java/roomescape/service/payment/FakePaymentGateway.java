package roomescape.service.payment;

import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.service.payment.port.PaymentGateway;

public class FakePaymentGateway implements PaymentGateway {

    private PaymentConfirmation requestedConfirmation;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        this.requestedConfirmation = confirmation;
        return new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
    }

    public PaymentConfirmation requestedConfirmation() {
        return requestedConfirmation;
    }
}
