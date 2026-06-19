package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.TossPaymentException;
import roomescape.repository.ReservationRepository;

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
        var reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다: " + orderId));

        if (!reservation.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(reservation.getAmount(), amount);
        }

        var confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        try {
            var result = paymentGateway.confirm(confirmation);
            reservation.completePayment(result.paymentKey());
            reservationRepository.updatePayment(reservation.getId(), result.paymentKey(), reservation.getStatus(),
                    reservation.getOrderId(), reservation.getAmount());
            return result;
        } catch (TossPaymentException.AlreadyProcessed e) {
            reservation.completePayment(paymentKey);
            reservationRepository.updatePayment(reservation.getId(), paymentKey, reservation.getStatus(),
                    reservation.getOrderId(), reservation.getAmount());
            return new PaymentResult(paymentKey, orderId, PaymentStatus.DONE, amount);
        } catch (TossPaymentException.Retryable e) {
            reservation.markUncertain();
            reservationRepository.updatePayment(reservation.getId(), paymentKey, reservation.getStatus(),
                    reservation.getOrderId(), reservation.getAmount());
            throw e;
        }
    }
}
