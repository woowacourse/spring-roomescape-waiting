package roomescape.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeDao extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

}
