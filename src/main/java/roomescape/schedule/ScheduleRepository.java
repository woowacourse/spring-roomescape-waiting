package roomescape.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByDateAndReservationTime_IdAndTheme_Id(LocalDate date, Long reservationTimeId, Long themeId);

    boolean existsByReservationTimeAndTheme(ReservationTime reservationTime, Theme theme);
}
