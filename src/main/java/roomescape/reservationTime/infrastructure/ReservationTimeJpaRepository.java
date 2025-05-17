package roomescape.reservationTime.infrastructure;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;

@Repository
public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {
    Boolean existsByStartAt(LocalTime startAt);

}
