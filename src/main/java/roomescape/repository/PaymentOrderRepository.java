package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.PaymentOrder;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder order);
    PaymentOrder update(PaymentOrder order);
    Optional<PaymentOrder> findByOrderId(String orderId);
    List<PaymentOrder> findByName(String name);
    void deleteByOrderId(String orderId);
}
