package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationApplicationResult;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationQueryService {

    private final ReservationDetailDao reservationDetailDao;

    public List<ReservationApplicationResult> findAll() {
        return reservationDetailDao.findAll().stream()
                .map(ReservationApplicationResult::from)
                .toList();
    }

    public List<ReservationApplicationResult> findByName(String username) {
        return reservationDetailDao.findByName(username).stream()
                .map(ReservationApplicationResult::from)
                .toList();
    }
}
