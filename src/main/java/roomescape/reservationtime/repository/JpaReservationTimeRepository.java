package roomescape.reservationtime.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
