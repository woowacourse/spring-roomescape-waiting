package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;

public interface TimeRepository extends JpaRepository<ReservationTime, Long> {
}
