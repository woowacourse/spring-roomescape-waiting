package roomescape.repository;

import roomescape.domain.PaymentOrder;

import java.util.Optional;

public interface PaymentOrderRepository {

    void save(PaymentOrder order);

    Optional<PaymentOrder> findByOrderId(String orderId);

    Optional<PaymentOrder> findByReservationId(long reservationId);

    void recordPaymentKey(String orderId, String paymentKey);

    void complete(String orderId, String paymentKey);

    void markUnknown(String orderId);

    void markFailed(String orderId);
}
