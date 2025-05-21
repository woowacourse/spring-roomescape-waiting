package roomescape.reservation.infrastructure;

import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Waiting;

public interface JpaWaitingRepository extends CrudRepository<Waiting, Long> {

    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);
}
