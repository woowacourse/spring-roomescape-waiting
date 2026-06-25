package roomescape.payment.order;

import java.util.List;

public interface PaymentOrderRepository {

    void save(PaymentOrder order);

    PaymentOrder getByOrderId(String orderId);

    List<PaymentOrderHistory> findHistoriesByReserverName(String reserverName);

    void markConfirmed(String orderId, String paymentKey, Long reservationId);

    void markCanceled(String orderId);

    void markUnknown(String orderId);
}
