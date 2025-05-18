package roomescape.persistence;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {
}
