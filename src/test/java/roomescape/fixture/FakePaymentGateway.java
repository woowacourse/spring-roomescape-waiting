package roomescape.fixture;

import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;

/**
 * 테스트용 결제 게이트웨이 더블. 외부 토스 호출 없이 항상 성공 응답을 돌려준다.
 */
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        return new PaymentResult(
                confirmation.paymentKey(),
                confirmation.orderId(),
                "DONE",
                confirmation.amount()
        );
    }

    @Override
    public String clientKey() {
        return "test_ck_fake";
    }
}
