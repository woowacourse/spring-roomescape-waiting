package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.time.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findByIdNotIn(List<Long> ids);

    boolean existsByStartAt(LocalTime time);
}
