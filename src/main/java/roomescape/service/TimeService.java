package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.TimeRequest;
import roomescape.exception.custom.DuplicatedException;

@Service
public class TimeService {

    private final ReservationTimeDao reservationTimeDao;

    public TimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAllTimes();
    }

    public List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId) {
        return reservationTimeDao.findAllTimesWithBooked(date, themeId);
    }

    public ReservationTime addReservationTime(TimeRequest request) {
        validateDuplicateTime(request);
        return reservationTimeDao.addTime(new ReservationTime(request.startAt()));
    }

    private void validateDuplicateTime(TimeRequest request) {
        if (reservationTimeDao.existTimeByStartAt(request.startAt())) {
            throw new DuplicatedException("reservationTime");
        }
    }

    public void removeReservationTime(Long id) {
        reservationTimeDao.removeTimeById(id);
    }
}
