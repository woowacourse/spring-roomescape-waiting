package roomescape.payment.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder savePending(
            String name,
            LocalDate date,
            Long themeId,
            Long timeId,
            String orderId,
            long amount,
            String idempotencyKey
    );

    Optional<PaymentOrder> findByOrderId(String orderId);

    List<PaymentOrderDetail> findAllByName(String name);

    void confirm(String orderId, String paymentKey);

    void markConfirmationUnknown(String orderId, String paymentKey);

    void keepPendingWithPaymentKey(String orderId, String paymentKey);

    void markFailed(String orderId);
}
