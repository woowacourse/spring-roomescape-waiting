package roomescape.payment.domain;

import java.time.LocalDate;
import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder savePending(
            String name,
            LocalDate date,
            Long themeId,
            Long timeId,
            String orderId,
            long amount
    );

    Optional<PaymentOrder> findByOrderId(String orderId);

    void confirm(String orderId, String paymentKey);

    void deletePending(String orderId);
}
