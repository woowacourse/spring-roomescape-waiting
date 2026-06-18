package roomescape.fixture;

import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentGatewayUnreachableException;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentResultUnknownException;

/**
 * 테스트용 결제 게이트웨이 더블. 기본은 항상 성공이고, 특정 sentinel paymentKey로 전송 실패를 흉내 낸다.
 */
public class FakePaymentGateway implements PaymentGateway {

    /** 이 paymentKey로 confirm하면 read timeout(결과 불명확)을 흉내 내 PaymentResultUnknownException을 던진다. */
    public static final String READ_TIMEOUT_KEY = "pk-read-timeout";
    /** 이 paymentKey로 confirm하면 연결 실패(확실히 안 됨)를 흉내 내 PaymentGatewayUnreachableException을 던진다. */
    public static final String CONNECT_FAIL_KEY = "pk-connect-fail";

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        String paymentKey = confirmation.paymentKey();
        if (READ_TIMEOUT_KEY.equals(paymentKey)) {
            throw new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다.", null);
        }
        if (CONNECT_FAIL_KEY.equals(paymentKey)) {
            throw new PaymentGatewayUnreachableException("결제 서버에 연결하지 못했습니다.", null);
        }
        return new PaymentResult(
                paymentKey,
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
