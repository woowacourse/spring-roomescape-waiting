package roomescape.repository;

import roomescape.domain.PendingPayment;

import java.util.Optional;

public interface PendingPaymentRepository {
    PendingPayment save(PendingPayment pendingPayment);
    Optional<PendingPayment> findByOrderId(String orderId);
    void deleteByOrderId(String orderId);
}
