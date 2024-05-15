package roomescape.reservation.repository;

import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public interface ReservationTimeJpaRepository extends CrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime localTime);
}
