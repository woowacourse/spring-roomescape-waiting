package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

}
