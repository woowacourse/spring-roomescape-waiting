package roomescape.waiting.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;

@Repository
public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {
    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);
}
