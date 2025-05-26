package roomescape.reservationtime.infrastructure;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);
}
