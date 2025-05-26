package roomescape.reservation.domain.repository;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);
}
