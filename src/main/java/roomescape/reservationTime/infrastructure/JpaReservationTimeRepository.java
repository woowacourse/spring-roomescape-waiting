package roomescape.reservationTime.infrastructure;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservationTime.domain.ReservationTime;

public interface JpaReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();
}
