package roomescape.waiting.infrastructure;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.waiting.domain.Waiting;

public interface JpaWaitingRepository extends ListCrudRepository<Waiting, Long> {

}
