package roomescape.reservation.application.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;

@Repository
public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

}
