package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    Optional<ReservationWaiting> findFirstBySlotOrderByIdAsc(Slot slot);
}
