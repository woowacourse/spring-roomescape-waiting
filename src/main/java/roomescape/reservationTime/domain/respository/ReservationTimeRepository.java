package roomescape.reservationTime.domain.respository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationTime.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    Boolean existsByStartAt(LocalTime startAt);
}
