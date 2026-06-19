package roomescape.persistence;

import roomescape.domain.payment.PaymentHistory;

public interface PaymentHistoryRepository {

    void save(PaymentHistory paymentHistory);
}
