package roomescape.reservation.repository.jpa;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime reservationTime);
}
