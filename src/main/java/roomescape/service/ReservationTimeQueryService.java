package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.reservation.ReservationTime;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final ReservationTimeDao reservationTimeDao;

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAllReservationTimes();
    }

    public List<ReservationTime> findAvailableReservationTimes(LocalDate date, Long themeId) {
        return reservationTimeDao.findAvailableReservationTimes(date, themeId);
    }
}
