package roomescape.core.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.core.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    Integer countByStartAt(final LocalTime startAt);

    void deleteById(final long id);
}
