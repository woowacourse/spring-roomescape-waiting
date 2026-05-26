package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationDetail;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationSearchCondition;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationQueryService {

    private final ReservationDetailDao reservationDetailDao;

    public List<ReservationResult> findAll(ReservationSearchCondition condition) {
        List<ReservationDetail> result = condition.hasUsername()
                ? reservationDetailDao.findByName(condition.username())
                : reservationDetailDao.findAll();

        return result.stream()
                .map(ReservationResult::from)
                .toList();
    }
}
