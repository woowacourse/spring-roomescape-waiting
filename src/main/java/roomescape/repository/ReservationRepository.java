package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation.Reservation;

public interface ReservationRepository {

    Long save(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Long> findThemeReservationCountsForDate(LocalDate startDate, LocalDate endDate);

    List<Reservation> findByDateAndTheme(LocalDate date, Long themeId);

    List<Reservation> findByMemberAndThemeAndDateRange(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    void delete(Long id);

    Boolean existTimeId(Long id);

    Boolean existThemeId(Long id);

    Boolean existDateTimeAndTheme(LocalDate date, Long timeId, Long ThemeId);

}
