package roomescape.domain.timeslot;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.BaseRepository;
import roomescape.exception.NotFoundException;

public interface TimeSlotRepository extends BaseRepository<TimeSlot, Long> {

    @Override
    TimeSlot save(TimeSlot timeSlot);

    @Override
    Optional<TimeSlot> findById(Long id);

    @Override
    TimeSlot getById(Long id) throws NotFoundException;

    List<TimeSlot> findAll();

    @Override
    List<TimeSlot> findAll(Specification<TimeSlot> specification);

    @Override
    boolean exists(Specification<TimeSlot> specification);

    @Override
    void delete(TimeSlot timeSlot);

    @Override
    void deleteByIdOrElseThrow(Long id) throws NotFoundException;
}
