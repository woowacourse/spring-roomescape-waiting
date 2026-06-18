package roomescape.payment;

import java.util.concurrent.atomic.AtomicInteger;
import roomescape.payment.application.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

/**
 * 실제 Toss 호출 없이 승인 성공을 흉내내는 테스트 더블. 호출 횟수를 기록해 "게이트웨이 미호출"을 검증할 수 있다.
 */
public class FakePaymentGateway implements PaymentGateway {

    private final AtomicInteger callCount = new AtomicInteger();

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        callCount.incrementAndGet();
        return new PaymentResult(
                "test_payment_key_" + confirmation.orderId(),
                confirmation.orderId(),
                "DONE",
                confirmation.amount()
        );
    }

    public int callCount() {
        return callCount.get();
    }
}
