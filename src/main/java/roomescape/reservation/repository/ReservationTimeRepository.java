package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findById(long id);

    int deleteById(long id);
}
