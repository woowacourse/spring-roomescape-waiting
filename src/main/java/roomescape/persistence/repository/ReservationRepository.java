package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
