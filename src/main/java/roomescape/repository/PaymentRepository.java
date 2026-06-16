package roomescape.repository;

import roomescape.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findByOrderId(String orderId);

    void updatePaymentKey(String orderId, String paymentKey);
}
