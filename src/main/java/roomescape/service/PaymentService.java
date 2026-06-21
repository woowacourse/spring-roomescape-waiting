package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Order;
import roomescape.domain.OrderId;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentStatus;
import roomescape.domain.ReservationStatus;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import roomescape.infrastructure.payment.PaymentUnknownException;
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

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = findOrder(orderId);
        validateAmount(order, amount);

        PaymentResult result = confirmWithGateway(order, paymentKey, amount);
        orderRepository.updatePayment(order.getOrderId(), result.status(), result.paymentKey());
        if (result.status() == PaymentStatus.DONE) {
            reservationRepository.updateStatus(order.getReservationId(), ReservationStatus.RESERVED);
        }
        return result;
    }

    private PaymentResult confirmWithGateway(Order order, String paymentKey, Long amount) {
        try {
            return paymentGateway.confirm(new PaymentConfirmation(paymentKey, order.getOrderId().getValue(), amount));
        } catch (PaymentUnknownException e) {
            orderRepository.updatePayment(order.getOrderId(), PaymentStatus.UNCONFIRMED, paymentKey);
            throw e;
        }
    }

    @Transactional
    public void cancelPendingOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            // 사용자가 결제창에서 취소(PAY_PROCESS_CANCELED)하면 orderId가 없을 수 있다.
            return;
        }
        orderRepository.findByOrderId(OrderId.of(orderId))
                .filter(order -> order.getStatus() == PaymentStatus.READY)
                .ifPresent(order -> {
                    orderRepository.deleteByOrderId(order.getOrderId());
                    reservationRepository.deleteById(order.getReservationId());
                });
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
