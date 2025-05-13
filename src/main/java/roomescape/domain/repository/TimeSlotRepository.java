package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.TimeSlot;

public interface TimeSlotRepository {

    Optional<TimeSlot> findById(long id);

    long save(TimeSlot timeSlot);

    boolean removeById(long id);

    List<TimeSlot> findAll();
}
