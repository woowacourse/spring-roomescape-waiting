package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.admin.domain.FilterInfo;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeExceptionCode;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.Time;
import roomescape.time.exception.TimeExceptionCode;

@Repository
public class ReservationRepository {

    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;

    public ReservationRepository(ReservationDao reservationDao, TimeDao timeDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
    }

    public Reservation save(Reservation reservation) {
        Time time = timeDao.findById(reservation.getReservationTime().getId())
                .orElseThrow(() -> new RoomEscapeException(TimeExceptionCode.FOUND_TIME_IS_NULL_EXCEPTION));
        Theme theme = themeDao.findById(reservation.getTheme().getId())
                .orElseThrow(() -> new RoomEscapeException(ThemeExceptionCode.FOUND_THEME_IS_NULL_EXCEPTION));

        Reservation saveReservation = Reservation.saveReservationOf(reservation.getDate(), time,
                theme, reservation.getMember());
        return reservationDao.save(saveReservation);
    }

    public List<Reservation> findAllReservationOrderByDateAndTimeStartAt() {
        return reservationDao.findAllReservationOrderByDateAndTimeStartAt();
    }

    public List<Reservation> findAllByThemeIdAndDate(long themeId, LocalDate date) {
        return reservationDao.findAllByThemeIdAndDate(themeId, date);
    }

    public List<Theme> findThemeByDateOrderByThemeIdCountLimit(LocalDate beforeWeek, LocalDate today, int limitCount) {
        return reservationDao.findThemeByDateOrderByThemeIdCountLimit(beforeWeek, today, limitCount);
    }

    public boolean existByTimeId(long timeId) {
        return reservationDao.existsByTimeId(timeId);
    }

    public boolean existByThemeId(long themeId) {
        return reservationDao.existsByThemeId(themeId);
    }

    public void deleteById(long reservationId) {
        reservationDao.deleteById(reservationId);
    }

    public List<Reservation> findAllByUserIdAndThemeIdBetweenDate(FilterInfo filterInfo) {
        return reservationDao.findAllByUserIdAndThemeIdBetweenDate(filterInfo.getMemberId(), filterInfo.getThemeId(),
                filterInfo.getFromDate(), filterInfo.getToDate());
    }
}
