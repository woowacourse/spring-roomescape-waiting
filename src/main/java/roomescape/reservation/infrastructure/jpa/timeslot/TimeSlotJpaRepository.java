package roomescape.reservation.infrastructure.jpa.timeslot;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.timeslot.TimeSlot;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByStartAt(LocalTime timeSlot);
}
