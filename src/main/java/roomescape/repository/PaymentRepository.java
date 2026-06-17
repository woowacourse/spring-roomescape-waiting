package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    void updatePaymentKey(String orderId, String paymentKey);

    void deleteByOrderId(String orderId);
}
