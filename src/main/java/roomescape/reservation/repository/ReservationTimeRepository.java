package roomescape.reservation.repository;

import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {
    int deleteById(long id);
}
