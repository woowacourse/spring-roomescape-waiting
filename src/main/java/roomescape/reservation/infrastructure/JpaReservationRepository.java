package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.infrastructure.projection.TimeValueProjection;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeAndThemeId(ReservationDate date, ReservationTime time, Long ThemeId);

    List<Reservation> findAllByUserId(UserId userId);

    List<Reservation> findAllByDateAndThemeId(ReservationDate date, Long themeId);

    List<TimeValueProjection> findTimeByDateAndThemeId(ReservationDate date, Long themeId);
}

