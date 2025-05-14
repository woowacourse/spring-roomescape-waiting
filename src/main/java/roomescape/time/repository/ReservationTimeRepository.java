package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();

    boolean existsByStartAt(LocalTime reservationTime);
}
