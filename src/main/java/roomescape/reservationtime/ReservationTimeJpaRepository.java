package roomescape.reservationtime;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);
}
