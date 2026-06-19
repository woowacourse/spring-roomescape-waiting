package roomescape.fake;

import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;

public class FakePaymentGateway implements PaymentGateway {

    private PaymentResult result;
    private RuntimeException exceptionToThrow;
    private boolean called = false;

    public void setResult(PaymentResult result) {
        this.result = result;
    }

    public void setException(RuntimeException exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
    }

    public boolean isCalled() {
        return called;
    }

    @Override
    public PaymentResult confirm(String paymentKey, String orderId, long amount) {
        this.called = true;
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        return result;
    }
}
