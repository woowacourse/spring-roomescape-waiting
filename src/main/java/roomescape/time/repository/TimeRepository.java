package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.Time;

@Repository
public class TimeRepository {

    private final TimeDao timeDao;

    public TimeRepository(TimeDao timeDao) {
        this.timeDao = timeDao;
    }

    public Time save(Time time) {
        return timeDao.save(time);
    }

    public List<Time> findAllReservationTimesInOrder() {
        return timeDao.findAllReservationTimesInOrder();
    }

    public List<Time> findAllReservationTimes() {
        return timeDao.findAllReservationTimesInOrder();
    }

    public Optional<Time> findByStartAt(LocalTime startAt) {
        return timeDao.findByStartAt(startAt);
    }

    public void deleteById(long reservationTimeId) {
        timeDao.deleteById(reservationTimeId);
    }
}
