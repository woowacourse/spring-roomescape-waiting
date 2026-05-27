package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.WaitingDetailDao;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationSearchCondition;
import roomescape.reservation.application.dto.WaitingDetail;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WaitingQueryService {

    private final WaitingDetailDao waitingDetailDao;

    public List<ReservationResult> findByName(ReservationSearchCondition condition) {
        List<WaitingDetail> result = waitingDetailDao.findByName(condition.username());

        return result.stream()
                .map(ReservationResult::from)
                .toList();
    }
}
