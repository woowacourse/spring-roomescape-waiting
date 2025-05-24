package roomescape.reservation.time.application;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.time.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);
}
