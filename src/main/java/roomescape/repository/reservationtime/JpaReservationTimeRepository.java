package roomescape.repository.reservationtime;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;

@Repository
public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByTime(LocalTime time);
}
