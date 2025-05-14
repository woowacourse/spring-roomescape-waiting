package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    List<Reservation> findByDateAndTheme(LocalDate date, Long themeId);
}
