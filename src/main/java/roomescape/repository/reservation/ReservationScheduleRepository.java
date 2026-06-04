package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;

public interface ReservationScheduleRepository {

    boolean existsByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    boolean existsByDateAndThemeIdAndTimeIdExcludingId(LocalDate date, long themeId, long timeId, long reservationId);

    List<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, long themeId);

    Optional<Reservation> findByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);
}
