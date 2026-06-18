package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.dao.PaymentOrderDao;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentResult;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentOrderDao paymentOrderDao;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentOrderDao paymentOrderDao, PaymentGateway paymentGateway) {
        this.paymentOrderDao = paymentOrderDao;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder paymentOrder = paymentOrderDao.selectByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!paymentOrder.getAmount().equals(amount)) {
            throw new RoomEscapeException(PaymentErrorCode.AMOUNT_MISMATCH);
        }

        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        return paymentGateway.confirm(confirmation);
    }
}
