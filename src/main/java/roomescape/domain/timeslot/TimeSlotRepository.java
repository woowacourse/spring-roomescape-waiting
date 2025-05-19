package roomescape.domain.timeslot;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.exception.NotFoundException;

public interface TimeSlotRepository extends ListCrudRepository<TimeSlot, Long> {

    default TimeSlot getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 타임 슬롯입니다. id : " + id));
    }
}
