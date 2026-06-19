package roomescape.payment.repository;

import roomescape.payment.domain.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    boolean update(Payment payment);
}
