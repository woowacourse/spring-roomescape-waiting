package roomescape.payment.repository;

import roomescape.payment.domain.PaymentOrder;

import java.util.Optional;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder paymentOrder);

    PaymentOrder update(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);
}
