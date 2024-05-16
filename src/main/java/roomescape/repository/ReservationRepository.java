package roomescape.repository;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Duration;
import roomescape.domain.Reservation;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

@Repository
public class ReservationRepository {
    private final ReservationDao reservationDao;

    public ReservationRepository(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public Reservation save(Reservation reservation) {
        return reservationDao.save(reservation);
    }

    public Reservations findAll(){
        return new Reservations(reservationDao.findAll());
    }

    public Reservations findAllByMemberId(long memberId) {
        return new Reservations(reservationDao.findAllByMemberId(memberId));
    }

    public Reservations findByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                    LocalDate dateTo) {
        if (themeId != null && memberId != null) {
            return new Reservations(
                    reservationDao.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo));
        }
        if (themeId != null) {
            return new Reservations(reservationDao.findByThemeIdAndDateBetween(themeId, dateFrom, dateTo));
        }
        if (memberId != null) {
            return new Reservations(reservationDao.findByMemberIdAndDateBetween(memberId, dateFrom, dateTo));
        }
        return new Reservations(reservationDao.findByDateBetween(dateFrom, dateTo));
    }

    public Reservations findByThemeAndDate(Theme theme, LocalDate date) {
        return new Reservations(reservationDao.findByThemeAndDate(theme, date));
    }

    public Themes findAndOrderByPopularity(Duration duration, int count) {
        return new Themes(reservationDao.findAndOrderByPopularity(duration.getStartDate(), duration.getEndDate(), count));
    }

    public boolean existsByTimeId(long timeId) {
        return reservationDao.existsByTimeId(timeId);
    }

    public boolean existsByThemeId(long themeId) {
        return reservationDao.existsByThemeId(themeId);
    }

    public void deleteById(long id) {
        reservationDao.deleteById(id);
    }
}
