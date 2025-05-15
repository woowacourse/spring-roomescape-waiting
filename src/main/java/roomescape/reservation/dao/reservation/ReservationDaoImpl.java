package roomescape.reservation.dao.reservation;

import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ReservationDaoImpl implements ReservationDao {

    private final JpaReservationDao jpaReservationDao;

    public ReservationDaoImpl(JpaReservationDao jpaReservationDao) {
        this.jpaReservationDao = jpaReservationDao;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationDao.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationDao.findAll();
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationDao.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate) {
        return jpaReservationDao.findByMemberIdAndThemeIdAndDateBetween(memberId, themeId, startDate, endDate);
    }

    @Override
    public int deleteById(Long id) {
        int deleteCount = jpaReservationDao.countById(id);
        if (deleteCount == 0) {
            return deleteCount;
        }
        jpaReservationDao.deleteById(id);
        return deleteCount;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return jpaReservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
