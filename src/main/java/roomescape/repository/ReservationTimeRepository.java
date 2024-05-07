package roomescape.repository;

import org.springframework.stereotype.Repository;
import roomescape.model.ReservationTime;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ReservationTimeDao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationTimeRepository {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeRepository(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAll();
    }

    public boolean isExistReservationTimeByStartAt(LocalTime startAt) {
        return reservationTimeDao.isExistByStartAt(startAt);
    }

    public Optional<ReservationTime> saveReservationTime(ReservationTime reservationTime) {
        long id = reservationTimeDao.save(reservationTime);
        return reservationTimeDao.findById(id);
    }

    public Optional<ReservationTime> findReservationById(long id) {
        return reservationTimeDao.findById(id);
    }

    public boolean isExistReservationTimeById(long id) {
        return reservationTimeDao.isExistById(id);
    }

    public boolean isExistReservationByTimeId(long timeId) {
        return reservationDao.isExistByTimeId(timeId);
    }

    public void deleteReservationTimeById(long id) {
        reservationTimeDao.deleteById(id);
    }
}
