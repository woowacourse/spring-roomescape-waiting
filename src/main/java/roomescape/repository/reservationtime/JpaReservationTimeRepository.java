package roomescape.repository.reservationtime;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservationtime.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByTime(LocalTime time);
}
