package roomescape.payment.repository;

import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderDetails;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder paymentOrder);

    PaymentOrder update(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);

    List<PaymentOrderDetails> findDetailsByName(String name);
}
