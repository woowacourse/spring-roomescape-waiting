package roomescape.domain.schedule;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Optional<Schedule> findByDateAndTime(ReservationDate reservationDate, ReservationTime reservationTime);
}
