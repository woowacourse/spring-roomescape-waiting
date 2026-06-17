package roomescape.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(ReservationRepository reservationRepository, PaymentGateway paymentGateway) {
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ReservationNotFoundException(-1L));

        if (!reservation.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(reservation.getAmount(), amount);
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        reservationRepository.confirmPayment(reservation.getId(), result.paymentKey());
        return result;
    }
}