package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.jpa.JpaReservationDao;
import roomescape.repository.jpa.JpaReservationTimeDao;

@Repository
public class JpaReservationTimeRepository implements ReservationTimeRepository {
    private final JpaReservationTimeDao jpaReservationTimeDao;
    private final JpaReservationDao jpaReservationDao;

    public JpaReservationTimeRepository(JpaReservationTimeDao jpaReservationTimeDao,
                                        JpaReservationDao jpaReservationDao) {
        this.jpaReservationTimeDao = jpaReservationTimeDao;
        this.jpaReservationDao = jpaReservationDao;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return jpaReservationTimeDao.save(reservationTime);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return jpaReservationTimeDao.existsByStartAt(startAt);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return jpaReservationTimeDao.findById(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeDao.findAll();
    }

    @Override
    public List<ReservationTime> findUsedTimeByDateAndTheme(LocalDate date, Theme theme) {
        return jpaReservationDao.findAllByDateAndTheme_Id(date, theme.getId())
                .stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    @Override
    public void delete(long id) {
        jpaReservationTimeDao.deleteById(id);
    }
}
