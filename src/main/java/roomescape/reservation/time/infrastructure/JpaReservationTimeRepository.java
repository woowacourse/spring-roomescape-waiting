package roomescape.reservation.time.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.time.domain.ReservationTime;

import java.time.LocalTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
