package roomescape.payment;

import java.util.concurrent.atomic.AtomicInteger;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.application.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentErrorCode;

/**
 * 실제 Toss 호출 없이 승인 성공/실패를 흉내내는 테스트 더블. 호출 횟수를 기록해 "게이트웨이 미호출"을 검증할 수 있다.
 */
public class FakePaymentGateway implements PaymentGateway {

    private final AtomicInteger callCount = new AtomicInteger();
    private PaymentErrorCode failure = null;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        callCount.incrementAndGet();
        if (failure != null) {
            throw new RoomEscapeException(failure);
        }
        return new PaymentResult(
                "test_payment_key_" + confirmation.orderId(),
                confirmation.orderId(),
                "DONE",
                confirmation.amount()
        );
    }

    public void willFailWith(PaymentErrorCode errorCode) {
        this.failure = errorCode;
    }

    public void willSucceed() {
        this.failure = null;
    }

    public void reset() {
        callCount.set(0);
        failure = null;
    }

    public int callCount() {
        return callCount.get();
    }
}
