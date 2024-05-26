package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
