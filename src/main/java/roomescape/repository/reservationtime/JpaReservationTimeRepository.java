package roomescape.repository.reservationtime;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationtime.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    boolean existsByTime(LocalTime time);
}
