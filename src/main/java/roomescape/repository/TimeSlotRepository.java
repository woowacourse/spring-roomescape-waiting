package roomescape.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByStartAt(LocalTime startAt);

    default TimeSlot getTimeSlotById(long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간 입니다"));
    }
}
