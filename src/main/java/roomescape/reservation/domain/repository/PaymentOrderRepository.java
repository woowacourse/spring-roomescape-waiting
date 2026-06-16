package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.PaymentOrder;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);

    Integer confirm(PaymentOrder paymentOrder);
}
