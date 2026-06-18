package roomescape.payment.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.TossPaymentException;
import roomescape.payment.controller.PaymentHistoryResponse;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderStatus;
import roomescape.payment.order.dao.PaymentOrderDao;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentOrderDao paymentOrderDao;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentOrderDao paymentOrderDao, PaymentGateway paymentGateway) {
        this.paymentOrderDao = paymentOrderDao;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder order = paymentOrderDao.selectByOrderId(orderId);
        if (!order.amount().equals(amount)) {
            throw new RoomescapeException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        // 멱등키는 클라이언트 입력이 아니라, 주문 생성 시 저장해 둔 값을 사용한다.
        // success 새로고침/재시도로 confirm 이 여러 번 와도 항상 같은 키가 실려 토스가 중복을 인지한다.
        PaymentConfirmation confirmation =
                new PaymentConfirmation(paymentKey, orderId, amount, order.idempotencyKey());
        try {
            PaymentResult result = paymentGateway.confirm(confirmation);
            paymentOrderDao.updateConfirmed(orderId, result.paymentKey(), result.approvedAmount());
            return result;
        } catch (TossPaymentException e) {
            // 토스가 명확히 거절 → 실패로 확정
            paymentOrderDao.updateStatus(orderId, PaymentOrderStatus.FAILED);
            throw e;
        } catch (PaymentResultUnknownException e) {
            // read timeout → 승인 여부 불명. "실패"가 아니라 "확인 필요"로 남겨 재확인/재시도 가능하게 둔다.
            paymentOrderDao.updateStatus(orderId, PaymentOrderStatus.UNKNOWN);
            throw e;
        } catch (PaymentConnectionException e) {
            // 연결 실패(요청 미도달) → 결제가 생성되지 않았으므로 PENDING 그대로 두고 재시도에 맡긴다.
            throw e;
        }
    }

    public List<PaymentHistoryResponse> findHistoryByName(String name) {
        return paymentOrderDao.selectHistoryByName(name);
    }
}
