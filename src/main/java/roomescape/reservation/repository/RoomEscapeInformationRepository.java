package roomescape.reservation.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.RoomEscapeInformation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface RoomEscapeInformationRepository extends JpaRepository<RoomEscapeInformation, Long> {

    boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme);

    boolean existsByThemeId(final Long themeId);

    boolean existsByTimeId(final Long timeId);
}
