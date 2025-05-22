package roomescape.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByDateAndReservationTime_IdAndTheme_Id(LocalDate date, Long reservationTimeId, Long themeId);
}
