package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.dto.ReservationApplicationSearchCondition;
import roomescape.reservation.application.dto.ReservationDetail;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationQueryService {

    private final ReservationDetailDao reservationDetailDao;

    public List<ReservationApplicationResult> findAll(ReservationApplicationSearchCondition condition) {
        List<ReservationDetail> result = condition.hasUsername()
                ? reservationDetailDao.findByName(condition.username())
                : reservationDetailDao.findAll();

        return result.stream()
                .map(ReservationApplicationResult::from)
                .toList();
    }
}
