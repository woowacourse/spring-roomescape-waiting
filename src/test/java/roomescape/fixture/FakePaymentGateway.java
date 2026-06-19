package roomescape.fixture;

import java.util.ArrayList;
import java.util.List;
import roomescape.payment.PaymentApprovalStatus;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.exception.PaymentGatewayUnreachableException;
import roomescape.payment.PaymentResult;
import roomescape.payment.exception.PaymentResultUnknownException;

/**
 * 테스트용 결제 게이트웨이 더블. 기본은 항상 성공이고, 특정 sentinel paymentKey로 전송 실패를 흉내 낸다.
 */
public class FakePaymentGateway implements PaymentGateway {

    /** 이 paymentKey로 confirm하면 read timeout(결과 불명확)을 흉내 내 PaymentResultUnknownException을 던진다. */
    public static final String READ_TIMEOUT_KEY = "pk-read-timeout";
    /** 이 paymentKey로 confirm하면 연결 실패(확실히 안 됨)를 흉내 내 PaymentGatewayUnreachableException을 던진다. */
    public static final String CONNECT_FAIL_KEY = "pk-connect-fail";
    /** 이 paymentKey로 cancel하면 환불 결과 불명확을 흉내 내 PaymentResultUnknownException을 던진다(워커 재시도 검증). */
    public static final String REFUND_UNKNOWN_KEY = "pk-refund-unknown";

    /** reconciliation 조회(findStatus)가 돌려줄 값. 테스트에서 setReconcileStatus로 제어한다(기본 NOT_APPROVED). */
    private PaymentApprovalStatus reconcileStatus = PaymentApprovalStatus.NOT_APPROVED;
    /** cancel(환불)로 취소된 paymentKey 기록 — 환불이 실제로 호출됐는지 검증한다. */
    private final List<String> canceledPaymentKeys = new ArrayList<>();

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
    public PaymentApprovalStatus findStatus(String orderId) {
        return reconcileStatus;
    }

    public void setReconcileStatus(PaymentApprovalStatus reconcileStatus) {
        this.reconcileStatus = reconcileStatus;
    }

    /** 공유 빈이라 테스트 간 상태가 누수되지 않게 매 테스트 초기화한다(@BeforeEach). */
    public void reset() {
        this.reconcileStatus = PaymentApprovalStatus.NOT_APPROVED;
        this.canceledPaymentKeys.clear();
    }

    @Override
    public void cancel(String paymentKey, String idempotencyKey) {
        if (REFUND_UNKNOWN_KEY.equals(paymentKey)) {
            throw new PaymentResultUnknownException("환불 결과를 확인하지 못했습니다.", null);
        }
        canceledPaymentKeys.add(paymentKey);
    }

    public List<String> canceledPaymentKeys() {
        return canceledPaymentKeys;
    }

    @Override
    public String clientKey() {
        return "test_ck_fake";
    }
}
