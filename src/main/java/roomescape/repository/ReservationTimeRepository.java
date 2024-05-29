package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
