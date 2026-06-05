package roomescape.reservationhistory;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationHistoryService {

    private final ReservationHistoryDao reservationHistoryDao;

    public ReservationHistoryService(ReservationHistoryDao reservationHistoryDao) {
        this.reservationHistoryDao = reservationHistoryDao;
    }

    public List<ReservationHistory> getMyHistory(Long memberId) {
        return reservationHistoryDao.findByMemberId(memberId);
    }

    public List<ReservationHistory> getStoreHistory(Long storeId) {
        return reservationHistoryDao.findByStoreId(storeId);
    }
}
