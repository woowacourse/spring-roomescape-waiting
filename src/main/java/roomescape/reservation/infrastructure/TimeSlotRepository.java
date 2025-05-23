package roomescape.reservation.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.reservation.domain.TimeSlot;

public interface TimeSlotRepository extends Repository<TimeSlot, Long> {
    List<TimeSlot> findAll();

    TimeSlot save(TimeSlot timeSlot);

    void deleteById(Long id);

    Optional<TimeSlot> findById(Long id);
}
