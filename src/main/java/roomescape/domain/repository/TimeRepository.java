package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.entity.ReservationTime;

public interface TimeRepository extends JpaRepository<ReservationTime, Long> {
}
