package roomescape.reservationTime.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationTime.domain.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
