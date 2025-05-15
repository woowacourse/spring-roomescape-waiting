package roomescape.repository;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.ReservationTime;

public interface ReservationTimeListCrudRepository extends ListCrudRepository<ReservationTime, Long> {

}
