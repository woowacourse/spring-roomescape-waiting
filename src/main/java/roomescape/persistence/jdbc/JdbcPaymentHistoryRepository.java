package roomescape.persistence.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.PaymentHistory;
import roomescape.persistence.PaymentHistoryRepository;
import roomescape.persistence.jdbc.dao.PaymentHistoryDao;

@Repository
@RequiredArgsConstructor
public class JdbcPaymentHistoryRepository implements PaymentHistoryRepository {

    private final PaymentHistoryDao paymentHistoryDao;

    @Override
    public void save(PaymentHistory paymentHistory) {
        paymentHistoryDao.save(paymentHistory);
    }
}
