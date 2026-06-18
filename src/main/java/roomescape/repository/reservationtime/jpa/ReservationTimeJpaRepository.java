package roomescape.repository.reservationtime.jpa;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTimeJpaEntity, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
