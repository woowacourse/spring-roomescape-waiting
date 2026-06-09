package roomescape.time.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ConflictException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeRepositoryImpl(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        try {
            return reservationTimeDao.save(reservationTime);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(TimeErrorCode.DUPLICATE_TIME);
        }
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimeDao.findById(id);
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
        try {
            reservationTimeDao.delete(time);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(TimeErrorCode.TIME_IN_USE);
        }
    }
}
