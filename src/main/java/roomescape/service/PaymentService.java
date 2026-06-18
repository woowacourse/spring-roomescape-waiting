package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Order;
import roomescape.domain.OrderId;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentStatus;
import roomescape.domain.ReservationStatus;
import roomescape.dto.payment.PaymentConfirmation;
import roomescape.dto.payment.PaymentResult;
import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationRepository;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(OrderRepository orderRepository, ReservationRepository reservationRepository,
                          PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = findOrder(orderId);
        validateAmount(order, amount);

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        orderRepository.updateStatus(order.getOrderId(), result.status());
        if (result.status() == PaymentStatus.DONE) {
            reservationRepository.updateStatus(order.getReservationId(), ReservationStatus.RESERVED);
        }
        return result;
    }

    private Order findOrder(String orderId) {
        return orderRepository.findByOrderId(OrderId.of(orderId))
                .orElseThrow(() -> new RoomescapeException(
                        ErrorType.RESOURCE_NOT_FOUND,
                        "주문을(를) 찾을 수 없습니다. orderId=" + orderId));
    }

    private void validateAmount(Order order, Long amount) {
        if (!order.getAmount().equals(amount)) {
            throw new RoomescapeException(
                    ErrorType.PAYMENT_AMOUNT_MISMATCH,
                    "결제 요청 금액이 주문 금액과 일치하지 않습니다. 주문 금액=" + order.getAmount() + ", 요청 금액=" + amount);
        }
    }
}
