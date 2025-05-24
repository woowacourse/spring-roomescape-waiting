package roomescape.repository;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.reservation.Waiting;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

}
