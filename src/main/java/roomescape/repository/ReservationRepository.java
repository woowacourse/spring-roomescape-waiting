package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByName(String name);

    List<Reservation> findByDateAndTime_IdAndTheme_IdOrderByRequestedAt(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);
}
