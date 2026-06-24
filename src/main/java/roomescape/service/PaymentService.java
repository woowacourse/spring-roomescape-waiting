package roomescape.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentResult;
import roomescape.repository.PaymentOrderRepository;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentOrderRepository paymentOrderRepository, PaymentGateway paymentGateway) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.NOT_FOUND));
        if (!Objects.equals(order.getAmount(), amount)) {
            throw new RoomEscapeException(PaymentErrorCode.AMOUNT_MISMATCH);
        }
        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        PaymentResult paymentResult = paymentGateway.confirm(confirmation);
        order.getReservation().updateStatus();

        return paymentResult;
    }
}
