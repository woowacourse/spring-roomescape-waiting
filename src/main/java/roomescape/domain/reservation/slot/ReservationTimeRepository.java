package roomescape.domain.reservation.slot;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
