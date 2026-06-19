package roomescape.repository;

import java.util.Optional;
import roomescape.domain.payment.PaymentOrder;

public interface PaymentOrderRepository {

    Long save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);

    int updatePaymentKey(String orderId, String paymentKey);

    int deleteByOrderId(String orderId);
}
