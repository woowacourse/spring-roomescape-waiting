package roomescape.time.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.TimeValue;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(TimeValue startAt);
}
