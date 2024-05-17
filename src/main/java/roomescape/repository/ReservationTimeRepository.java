package roomescape.repository;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimes;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

}
