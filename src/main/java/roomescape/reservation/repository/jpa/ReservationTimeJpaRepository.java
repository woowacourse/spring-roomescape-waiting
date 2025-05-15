package roomescape.reservation.repository.jpa;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime reservationTime);
}
