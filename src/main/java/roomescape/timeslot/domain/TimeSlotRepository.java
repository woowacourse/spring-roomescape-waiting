package roomescape.timeslot.domain;

import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository {

    boolean existsById(TimeSlotId id);

    boolean existsByStartAt(ReservationTime startAt);

    Optional<TimeSlot> findById(TimeSlotId id);

    List<TimeSlot> findAll();

    TimeSlot save(TimeSlot timeSlot);

    void deleteById(TimeSlotId id);
}
