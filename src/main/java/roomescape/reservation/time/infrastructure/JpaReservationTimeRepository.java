package roomescape.reservation.time.infrastructure;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.time.domain.ReservationTime;

public interface JpaReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();
}
