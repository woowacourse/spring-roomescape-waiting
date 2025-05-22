package roomescape.domain.timeslot;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.NotFoundException;

public interface TimeSlotRepository extends ListCrudRepository<TimeSlot, Long> {

    @Modifying
    @Query("DELETE FROM RESERVATION_TIME rt WHERE rt.id = :id")
    @Transactional
    int deleteByIdAndCount(@Param("id") long id);

    @Transactional
    default void deleteByIdOrElseThrow(final long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다. id : " + id);
        }
    }

    default TimeSlot getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 타임 슬롯입니다. id : " + id));
    }
}
