package roomescape.domain.repository;

import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentStatus;

public interface PaymentOrderRepository {
    PaymentOrder getByOrderId(String orderId);
    void save(PaymentOrder paymentOrder);
    void updateStatus(long id, PaymentStatus status);
}
