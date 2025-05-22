package roomescape.reservation.repository;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long>, ReservationTimeRepository {
    boolean existsByStartAt(LocalTime reservationTime);
}
