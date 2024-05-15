package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);
}
