package roomescape.reservation.infrastructure;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAllByStartAt(LocalTime startAt);
}
