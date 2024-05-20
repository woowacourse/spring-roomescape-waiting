package roomescape.domain.reservation.repository.reservationTime;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
