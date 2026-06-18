package roomescape.service.payment;

import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.infrastructure.payment.TossPaymentException;
import roomescape.service.payment.port.PaymentGateway;

public class FakePaymentGateway implements PaymentGateway {

    private PaymentConfirmation requestedConfirmation;
    private boolean failWithConfirmationUnknown;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        this.requestedConfirmation = confirmation;
        if (failWithConfirmationUnknown) {
            throw new TossPaymentException.ConfirmationUnknown(new RuntimeException("read timeout"));
        }
        return new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
    }

    public PaymentConfirmation requestedConfirmation() {
        return requestedConfirmation;
    }

    public void failWithConfirmationUnknown() {
        failWithConfirmationUnknown = true;
    }
}
