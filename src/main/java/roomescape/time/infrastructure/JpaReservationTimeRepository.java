package roomescape.time.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.time.domain.ReservationTime;

import java.time.LocalTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
