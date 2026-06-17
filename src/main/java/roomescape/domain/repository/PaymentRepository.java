package roomescape.domain.repository;

import java.util.Optional;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentStatus;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByReservationId(Long reservationId);

    void updateConfirmed(String orderId, String paymentKey, PaymentStatus status);

    void deleteByReservationId(Long reservationId);

    void updateStatus(String orderId, PaymentStatus status);
}
