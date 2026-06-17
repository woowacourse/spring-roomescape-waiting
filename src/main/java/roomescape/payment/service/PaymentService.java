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

    public PaymentResult confirm(PaymentConfirmation confirmation) {
        PaymentOrder order = paymentOrderDao.selectByOrderId(confirmation.orderId());
        if (!order.amount().equals(confirmation.amount())) {
            throw new RoomescapeException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        return paymentGateway.confirm(confirmation);
    }
}
