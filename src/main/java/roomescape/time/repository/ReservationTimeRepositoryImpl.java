package roomescape.time.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeRepositoryImpl(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeDao.save(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimeDao.findById(id);
    }

    @Override
    public boolean existsByStartAt(ReservationTime reservationTime) {
        return reservationTimeDao.existsByStartAt(reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeDao.findAll();
    }

    @Override
    public List<AvailableTimeQueryResult> queryAvailableTimes(long themeId, LocalDate date) {
        return reservationTimeDao.queryAvailableTimes(themeId, date);
    }

    @Override
    public void delete(ReservationTime time) {
        reservationTimeDao.delete(time);
    }
}
