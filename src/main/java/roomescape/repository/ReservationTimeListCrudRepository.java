package roomescape.repository;

import java.time.LocalTime;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeListCrudRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);
}
