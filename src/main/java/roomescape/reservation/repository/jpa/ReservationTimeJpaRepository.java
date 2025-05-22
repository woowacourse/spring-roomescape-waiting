package roomescape.reservation.repository.jpa;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long>, ReservationTimeRepository {
    boolean existsByStartAt(LocalTime reservationTime);
}
