package roomescape.domain.payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    Payment updatePaymentKey(String orderId, String paymentKey);
}
