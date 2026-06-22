package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(ReservationRepository reservationRepository,
                          PaymentRepository paymentRepository,
                          PaymentGateway paymentGateway) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return new PaymentResult(paymentKey, orderId, PaymentStatus.DONE, reservation.getAmount());
        }

        if (!reservation.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(reservation.getAmount(), amount);
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        paymentRepository.save(reservation.getId(), result.paymentKey(), result.approvedAmount());
        reservationRepository.confirm(reservation.getId());

        return result;
    }

    @Transactional
    public void cancelPendingReservation(String orderId) {
        reservationRepository.findByOrderId(orderId)
                .ifPresent(r -> reservationRepository.delete(r));
    }

    @Transactional
    public void markPaymentUnknown(String orderId) {
        reservationRepository.findByOrderId(orderId)
                .ifPresent(r -> reservationRepository.updateStatus(r.getId(), ReservationStatus.PAYMENT_UNKNOWN));
    }
}
