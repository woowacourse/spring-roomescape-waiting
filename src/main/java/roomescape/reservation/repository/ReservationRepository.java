package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Boolean existsByTimeId(Long timeId);

    Boolean existsByThemeId(Long themeId);

    List<Reservation> findByDateBetweenAndThemeId(LocalDate start, LocalDate end, Long themeId);
}
