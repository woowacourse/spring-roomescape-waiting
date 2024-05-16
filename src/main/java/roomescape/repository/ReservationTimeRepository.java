package roomescape.repository;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimes;

@Repository
public class ReservationTimeRepository {
    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeRepository(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeDao.save(reservationTime);
    }

    public ReservationTimes findAll() {
        return new ReservationTimes(reservationTimeDao.findAll());
    }

    public Optional<ReservationTime> findById(long id) {
        return reservationTimeDao.findById(id);
    }

    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimeDao.existsByStartAt(startAt);
    }

    public void deleteById(long id) {
        reservationTimeDao.deleteById(id);
    }
}
