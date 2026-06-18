package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.PaymentHistoryDao;
import roomescape.reservation.application.dto.PaymentHistoryResult;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PaymentQueryService {

    private final PaymentHistoryDao paymentHistoryDao;

    public List<PaymentHistoryResult> findByName(String username) {
        return paymentHistoryDao.findByName(username).stream()
                .map(PaymentHistoryResult::from)
                .toList();
    }
}
