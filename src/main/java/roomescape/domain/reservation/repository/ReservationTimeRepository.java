package roomescape.domain.reservation.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(@Param("startAt") LocalTime startAt);
}
