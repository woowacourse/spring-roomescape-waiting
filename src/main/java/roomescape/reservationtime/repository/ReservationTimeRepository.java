package roomescape.reservationtime.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
