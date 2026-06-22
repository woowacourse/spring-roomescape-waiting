package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.payment.Payment;
import roomescape.payment.PaymentOrderStatus;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByReservationId(Long reservationId);

    List<Payment> findByReservationIds(List<Long> reservationIds);

    void updatePaymentKey(String orderId, String paymentKey);

    void updateStatus(String orderId, PaymentOrderStatus status);

    void deleteByOrderId(String orderId);
}
