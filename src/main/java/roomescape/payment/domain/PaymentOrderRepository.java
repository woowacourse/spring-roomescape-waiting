package roomescape.payment.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder paymentOrder);

    PaymentOrder update(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);

    List<PaymentOrderDetails> findDetailsByName(String name);
}
