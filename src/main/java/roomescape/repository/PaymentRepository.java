package roomescape.repository;

import roomescape.domain.Payment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByReservationIds(Collection<Long> reservationIds);

    void updatePaymentKey(String orderId, String paymentKey);
}
