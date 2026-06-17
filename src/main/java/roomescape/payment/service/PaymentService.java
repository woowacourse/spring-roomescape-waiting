package roomescape.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.order.domain.Order;
import roomescape.order.repository.OrderRepository;
import roomescape.payment.client.PaymentGateway;
import roomescape.payment.service.dto.PaymentConfirmation;
import roomescape.payment.service.dto.PaymentResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.slot.repository.ReservationSlotRepository;

import java.util.List;
import java.util.UUID;

import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository slotRepository;

    public Order createOrder(Long reservationId, Long amount) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, reservationId, amount);
        return orderRepository.save(order);
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        var order = orderRepository.getByOrderId(orderId);
        order.validateAmountMatch(amount);

        PaymentResult confirmed = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        if (confirmed.isDone()) {
            confirm(getReservation(order.getReservationId())); // TODO Payment 저장.
            return confirmed;
        }

        throw new RuntimeException(); // 예외처리 ?
    }

    public void cancelPayment(String orderId) {
        Reservation reservation = getReservationByOrderId(orderId);
        if (reservation.isPendingPayment()) {
            ReservationSlot slot = getSlotAndReservationsWithLock(reservation.getSlotId());
            Reservations changed = slot.cancelByManager(reservation.getId());
            cancelAndPromote(changed);
        }
    }

    private void confirm(Reservation reservation) {
        reservation.promote();
        reservationRepository.updateStatus(reservation);
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorInformation.RESERVATION_NOT_FOUND));
    }

    public Reservation getReservationByOrderId(String orderId) {
        var order = orderRepository.getByOrderId(orderId);
        return getReservation(order.getReservationId());
    }

    private ReservationSlot getSlotAndReservationsWithLock(Long slotId) {
        ReservationSlot slot = getSlotWithLock(slotId);
        List<Reservation> activeReservations = getReservationsOfSlot(slot);
        return slot.withReservations(new Reservations(activeReservations));
    }

    private ReservationSlot getSlotWithLock(Long slotId) {
        return slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
    }

    private List<Reservation> getReservationsOfSlot(ReservationSlot slot) {
        return reservationRepository.findReservedAndWaitingBySlotId(slot.getId());
    }

    public void cancelAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateStatus);
    }

}
