package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Order;
import roomescape.domain.ReservationStatus;
import roomescape.exception.client.PaymentAmountMismatchException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationRepository;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            PaymentGateway paymentGateway
    ) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    public void confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        validateAmount(order, amount);
        paymentGateway.confirm(new PaymentConfirmation(
                paymentKey,
                orderId,
                amount,
                order.getIdempotencyKey()
        ));
        orderRepository.confirm(orderId, paymentKey);
        reservationRepository.updateStatus(order.getReservationId(), ReservationStatus.CONFIRMED);
    }

    public void fail(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        reservationRepository.deleteById(order.getReservationId());
    }

    private void validateAmount(Order order, Long amount) {
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
    }
}
