package roomescape.timeslot.infrastructure;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.timeslot.domain.TimeSlot;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByStartAt(LocalTime timeSlot);
}
