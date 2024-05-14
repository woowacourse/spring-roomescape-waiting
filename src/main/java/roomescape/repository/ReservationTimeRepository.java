package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Time;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    Boolean existsByStartAt(Time startTime);
}
