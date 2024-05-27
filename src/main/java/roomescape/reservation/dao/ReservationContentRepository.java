package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationContent;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public interface ReservationContentRepository extends JpaRepository<ReservationContent, Long> {

    Optional<ReservationContent> findByThemeAndTimeAndDate(Theme theme, Time time, LocalDate date);

}
