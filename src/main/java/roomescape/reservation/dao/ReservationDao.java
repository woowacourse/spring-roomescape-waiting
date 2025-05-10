package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.model.Reservation;

public interface ReservationDao {

    Reservation add(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndStartDateAndEndDate(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    int deleteById(Long id);

    boolean existByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId);
}
