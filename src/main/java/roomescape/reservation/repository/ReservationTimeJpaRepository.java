package roomescape.reservation.repository;

import java.time.LocalTime;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeJpaRepository extends CrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime localTime);
}
