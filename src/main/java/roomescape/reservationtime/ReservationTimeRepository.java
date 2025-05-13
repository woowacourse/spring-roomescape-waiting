package roomescape.reservationtime;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    Boolean existsByStartAt(LocalTime startAt);
}
