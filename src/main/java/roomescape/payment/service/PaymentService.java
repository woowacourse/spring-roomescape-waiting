package roomescape.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.client.PaymentGateway;
import roomescape.payment.domain.Payment;
import roomescape.payment.exception.TossPaymentException;
import roomescape.payment.repository.PaymentRepository;
import roomescape.payment.service.dto.PaymentConfirmation;
import roomescape.payment.service.dto.PaymentResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.exception.ReservationSlotException;
import roomescape.slot.repository.ReservationSlotRepository;

import java.util.List;
import java.util.UUID;

import static roomescape.slot.exception.ReservationSlotErrorInformation.SLOT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    @Transactional
    public Payment createPendingPayment(Long reservationId, Long slotId, Long amount) {
        String orderId = UUID.randomUUID().toString();
        return paymentRepository.save(Payment.pending(reservationId, slotId, orderId, amount));
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = findPayment(orderId);
        payment.validateAmountMatch(amount);

        try {
            PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
            payment.confirm(paymentKey);
            paymentRepository.update(payment);
            confirmReservation(payment.getSlotId(), payment.getReservationId());
            return result;
        } catch (TossPaymentException e) {
            if (e.isRetryable()) {
                payment.markUnknown();
                updatePaymentStatus(payment);
                throw e;
            }
            payment.fail();
            updatePaymentStatus(payment);
            cancelReservation(payment.getSlotId(), payment.getReservationId());
            throw e;
        } catch (Exception e) {
            payment.markUnknown();
            updatePaymentStatus(payment);
            throw e;
        }
    }

    @Transactional
    public void cancelPendingByOrderId(String orderId) {
        if (orderId == null) {
            return;
        }
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.fail();
            paymentRepository.update(payment);
            cancelReservation(payment.getSlotId(), payment.getReservationId());
        });
    }

    private void confirmReservation(Long slotId, Long reservationId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservation confirmed = slot.confirmPayment(reservationId);
        reservationRepository.updateStatus(confirmed);
    }

    private void cancelReservation(Long slotId, Long reservationId) {
        ReservationSlot slot = getSlotAndReservationsWithLock(slotId);
        Reservations changed = slot.cancelByManager(reservationId);
        cancelAndPromote(changed);
    }

    private ReservationSlot getSlotAndReservationsWithLock(Long slotId) {
        ReservationSlot slot = reservationSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new ReservationSlotException(SLOT_NOT_FOUND));
        List<Reservation> activeReservations = reservationRepository.findReservedAndWaitingBySlotId(slotId);
        return slot.withReservations(new Reservations(activeReservations));
    }

    private void updatePaymentStatus(Payment payment) {
        paymentRepository.update(payment);
    }

    private Payment findPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }

    public void cancelAndPromote(Reservations changed) {
        changed.values().forEach(reservationRepository::updateStatus);
    }

}
