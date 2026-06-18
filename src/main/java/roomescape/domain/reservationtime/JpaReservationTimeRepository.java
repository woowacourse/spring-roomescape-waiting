package roomescape.domain.reservationtime;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAllByOrderByStartAtAsc();
}
