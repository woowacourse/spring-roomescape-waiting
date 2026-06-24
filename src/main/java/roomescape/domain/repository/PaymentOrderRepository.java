package roomescape.domain.repository;

import roomescape.domain.PaymentOrder;

public interface PaymentOrderRepository {
    PaymentOrder getByOrderId(String orderId);
    void save(PaymentOrder paymentOrder);
}
