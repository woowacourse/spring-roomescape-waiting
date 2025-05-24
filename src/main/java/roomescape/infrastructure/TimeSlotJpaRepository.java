package roomescape.infrastructure;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.exception.NotFoundException;

public interface TimeSlotJpaRepository extends TimeSlotRepository, Repository<TimeSlot, Long> {

    default TimeSlot getById(final Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 타임 슬롯입니다. id : " + id));
    }

    @Modifying
    @Query("DELETE FROM RESERVATION_TIME rt WHERE rt.id = :id")
    @Transactional
    int deleteByIdAndCount(@Param("id") final Long id);

    @Transactional
    default void deleteByIdOrElseThrow(final Long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 타임 슬롯입니다. id : " + id);
        }
    }
}
