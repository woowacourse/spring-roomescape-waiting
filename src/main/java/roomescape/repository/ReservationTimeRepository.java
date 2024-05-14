package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.time.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

   List<ReservationTime> findByIdNotIn(List<Long> ids);

    boolean existsByStartAt(LocalTime time);
}
