package roomescape.timeslot.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.ReservationTime;

public interface JpaTimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByStartAt(ReservationTime startAt);
}
