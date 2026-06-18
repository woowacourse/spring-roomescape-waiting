package roomescape.payment.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.dao.PaymentOrderDao;

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
        return paymentGateway.confirm(confirmation);
    }
}
