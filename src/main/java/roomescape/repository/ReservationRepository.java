package roomescape.repository;

import java.time.LocalDate;
import roomescape.domain.Duration;
import roomescape.domain.Reservation;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    Reservations findAll();

    Reservations findByThemeAndDate(Theme theme, LocalDate date);

    Reservations searchBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    Themes findAndOrderByPopularity(Duration duration, int count);

    boolean existByTimeId(long timeId);

    boolean existByThemeId(long themeId);

    void delete(long id);
}
