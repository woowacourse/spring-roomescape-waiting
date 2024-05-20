package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationWait;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {
}
