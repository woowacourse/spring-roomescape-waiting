package roomescape.reservation.domain.repository;

import roomescape.reservation.domain.PaymentOrder;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder paymentOrder);
}
