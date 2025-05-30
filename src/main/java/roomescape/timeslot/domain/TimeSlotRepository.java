package roomescape.timeslot.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository {

    boolean existsByStartAt(LocalTime startAt);

    TimeSlot save(TimeSlot timeSlot);

    void deleteById(long id);

    List<TimeSlot> findAll();

    Optional<TimeSlot> findById(long id);
}
