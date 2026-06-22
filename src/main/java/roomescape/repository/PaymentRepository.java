package roomescape.repository;

import roomescape.domain.payment.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByReservationId(Long reservationId);
}
