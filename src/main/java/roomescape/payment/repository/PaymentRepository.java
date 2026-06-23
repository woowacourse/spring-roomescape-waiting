package roomescape.payment.repository;

import java.util.Optional;
import roomescape.payment.domain.Payment;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    void updatePaymentKey(String orderId, String paymentKey);
}
