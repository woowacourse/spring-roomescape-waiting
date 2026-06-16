package roomescape.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.order.domain.Order;
import roomescape.order.repository.OrderRepository;
import roomescape.payment.client.PaymentGateway;
import roomescape.payment.service.dto.PaymentConfirmation;
import roomescape.payment.service.dto.PaymentResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.repository.ReservationRepository;

import java.util.UUID;

import static roomescape.payment.service.dto.PaymentStatus.DONE;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;

    public Order createOrder(Long reservationId, Long amount) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, reservationId, amount);
        return orderRepository.save(order);
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        var order = orderRepository.getByOrderId(orderId);
        order.validateAmountMatch(amount);
        var confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        PaymentResult confirmed = paymentGateway.confirm(confirmation);
        if (confirmed.status() == DONE) {
            confirm(getReservation(order.getReservationId())); // TODO Payment 저장.
            return confirmed;
        }

        throw new RuntimeException(); // 예외처리
    }

    private void confirm(Reservation reservation) {
        reservation.promote();
        reservationRepository.updateStatus(reservation);
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorInformation.RESERVATION_NOT_FOUND));
    }

}
