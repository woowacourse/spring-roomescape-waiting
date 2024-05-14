package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.jpa.JpaReservationDao;

@Repository
public class JpaReservationRepository implements ReservationRepository {
    private final JpaReservationDao jpaReservationDao;

    public JpaReservationRepository(JpaReservationDao jpaReservationDao) {
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
    public List<Reservation> findByMemberAndThemeBetweenDates(long memberId, long themeId, LocalDate start,
                                                              LocalDate end) {
        return jpaReservationDao.findByReservationMember_IdAndTheme_IdAndDateBetween(memberId, themeId, start, end);
    }

    @Override
    public List<Reservation> findByMemberId(long memberId) {
        return jpaReservationDao.findAllByReservationMember_Id(memberId);
    }

    @Override
    public boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime) {
        return jpaReservationDao.existsByThemeAndDateAndTime(theme, date, reservationTime);
    }

    @Override
    public boolean existsByTime(ReservationTime reservationTime) {
        return jpaReservationDao.existsByTime(reservationTime);
    }

    @Override
    public boolean existsByTheme(Theme theme) {
        return jpaReservationDao.existsByTheme(theme);
    }

    @Override
    public void delete(long id) {
        jpaReservationDao.deleteById(id);
    }
}
