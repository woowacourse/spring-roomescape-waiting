package roomescape.reservationTime.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationTime.domain.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
