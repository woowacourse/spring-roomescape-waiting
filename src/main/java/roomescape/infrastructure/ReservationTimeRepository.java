package roomescape.infrastructure;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    
    boolean existsByStartAt(LocalTime parsedTime);
}
