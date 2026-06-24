package roomescape.domain;

import java.util.List;
import java.util.Optional;
import roomescape.domain.projection.ReservationWaitingWithOrder;

public interface ReservationWaitingQueryRepository {

    Optional<ReservationWaitingWithOrder> findById(Long id);

    List<ReservationWaitingWithOrder> findByMember(Member member);
}
