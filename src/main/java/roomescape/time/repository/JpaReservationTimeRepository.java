package roomescape.time.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
