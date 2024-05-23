package roomescape.waiting.repository;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservation_idAndMember_id(Long reservation_id, Long memberId);
}
