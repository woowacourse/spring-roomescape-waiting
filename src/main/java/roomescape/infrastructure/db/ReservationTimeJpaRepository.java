package roomescape.infrastructure.db;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.ReservationTime;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
