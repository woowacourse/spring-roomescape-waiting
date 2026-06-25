package roomescape.payment.order;

public interface PaymentOrderRepository {

    void save(PaymentOrder order);

    PaymentOrder getByOrderId(String orderId);

    void markConfirmed(String orderId, String paymentKey, Long reservationId);

    void markCanceled(String orderId);

    void markUnknown(String orderId);
}
