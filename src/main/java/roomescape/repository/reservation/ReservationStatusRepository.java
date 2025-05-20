package roomescape.repository.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationStatus;

public interface ReservationStatusRepository extends JpaRepository<ReservationStatus, Long> {
}
