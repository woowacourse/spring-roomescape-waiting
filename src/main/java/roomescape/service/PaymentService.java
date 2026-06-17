package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Order;
import roomescape.domain.ReservationStatus;
import roomescape.exception.client.PaymentAmountMismatchException;
import roomescape.infra.payment.TossPaymentClient;
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationRepository;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final TossPaymentClient tossPaymentClient;

    public PaymentService(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            TossPaymentClient tossPaymentClient
    ) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.tossPaymentClient = tossPaymentClient;
    }

    public void confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        validateAmount(order, amount);
        tossPaymentClient.confirm(paymentKey, orderId, amount);
        orderRepository.confirm(orderId, paymentKey);
        reservationRepository.updateStatus(order.getReservationId(), ReservationStatus.CONFIRMED);
    }

    public void fail(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        reservationRepository.updateStatus(order.getReservationId(), ReservationStatus.FAILED);
    }

    private void validateAmount(Order order, Long amount) {
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
    }
}
