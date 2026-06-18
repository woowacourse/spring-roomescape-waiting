package roomescape.payment.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.repository.ReservationRepository;

@Component
public class PaymentResultPersister {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentResultPersister(PaymentRepository paymentRepository, ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void persistConfirmed(Long reservationId, String orderId, String paymentKey) {
        paymentRepository.confirm(orderId, paymentKey);
        reservationRepository.confirm(reservationId);
    }

    @Transactional
    public void persistUnknown(String orderId, String paymentKey) {
        paymentRepository.markUnknown(orderId, paymentKey);
    }
}