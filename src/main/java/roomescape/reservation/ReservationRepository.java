package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    Long save(Reservation reservation, Long reservationTimeId, Long themeId, Long memberId);

    Reservation findById(Long id);
    List<Reservation> findAll();
    List<Reservation> findAllByThemeIdAndDate(Long themeId, LocalDate date);
    List<Reservation> findAllByMemberIdAndThemeIdAndDateRange(Long memberId, Long themeId, LocalDate from, LocalDate to);

    void delete(Long id);

    Boolean existsById(Long id);
    Boolean existsByReservationTime(Long reservationTimeId);
    Boolean existsByTheme(Long themeId);
    Boolean existsByReservationTimeIdAndDateAndThemeId(Long reservationTimeId, LocalDate date, Long themeId);
}
