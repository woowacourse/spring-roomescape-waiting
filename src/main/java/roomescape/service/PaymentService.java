package roomescape.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.domain.Order;
import roomescape.dto.request.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.dto.response.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.repository.OrderRepository;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.NOT_FOUND));
        if (!Objects.equals(order.getAmount(), amount)) {
            throw new RoomEscapeException(PaymentErrorCode.AMOUNT_MISMATCH);
        }
        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount, order.getIdempotencyKey());
        PaymentResult paymentResult = paymentGateway.confirm(confirmation);
        order.updatePaymentKey(paymentResult.paymentKey());
        if (paymentResult.status() == PaymentStatus.DONE) {
            order.getReservation().updateStatus();
        }

        return paymentResult;
    }
}
