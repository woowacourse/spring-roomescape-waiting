package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    void delete(long reservationId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsBy(LocalDate date, ReservationTime time, Theme theme);
}
