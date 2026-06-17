package roomescape.feature.payment.repository;

import java.util.List;
import java.util.Optional;
import roomescape.feature.payment.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByReservationIds(List<Long> reservationIds);
}
