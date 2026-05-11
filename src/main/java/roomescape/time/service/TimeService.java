package roomescape.time.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.AvailableTime;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;

@Service
public class TimeService {

    private final ReservationDao reservationDao;
    private final TimeDao timeDao;

    public TimeService(ReservationDao reservationDao, TimeDao timeDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
    }

    public List<ReservationTime> findAll() {
        return timeDao.selectAll();
    }

    public List<AvailableTime> findByThemeIdAndDate(Long themeId, LocalDate date) {
        List<ReservationTime> times = timeDao.selectAll();
        List<Long> bookedTimeIds = reservationDao.findTimeIdByThemeIdAndDate(themeId, date);

        return times.stream()
                .map(time -> new AvailableTime(time.getStartAt(), !bookedTimeIds.contains(time.getId())))
                .toList();
    }

    public ReservationTime add(LocalTime startAt) {
        ReservationTime time = new ReservationTime(startAt);
        return timeDao.insert(time);
    }

    public void delete(Long id) {
        timeDao.delete(id);
    }
}
