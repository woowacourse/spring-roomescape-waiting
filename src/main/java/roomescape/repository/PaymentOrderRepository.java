package roomescape.repository;

import java.util.Optional;
import roomescape.domain.PaymentOrder;

public interface PaymentOrderRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    void update(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);
}
