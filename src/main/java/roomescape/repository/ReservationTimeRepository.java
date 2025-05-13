package roomescape.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();
}
