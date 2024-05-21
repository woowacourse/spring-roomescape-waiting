package roomescape.reservation.repository;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
}
