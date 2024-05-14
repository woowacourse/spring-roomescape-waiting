package roomescape.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
