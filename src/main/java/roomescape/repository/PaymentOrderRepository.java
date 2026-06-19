package roomescape.repository;

import java.util.Optional;
import roomescape.domain.PaymentOrder;

public interface PaymentOrderRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);
}
