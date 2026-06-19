package roomescape.payment.repository;

import java.util.Optional;
import roomescape.payment.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Payment getByOrderId(String orderId);

    Optional<Payment> findByReservationId(Long reservationId);

    void confirm(String orderId, String paymentKey);

    void deleteByOrderId(String orderId);
}