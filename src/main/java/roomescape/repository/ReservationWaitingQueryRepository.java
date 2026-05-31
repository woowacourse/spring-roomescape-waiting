package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.projection.ReservationWaitingWithOrder;

public interface ReservationWaitingQueryRepository {

    Optional<ReservationWaitingWithOrder> findById(Long id);

    List<ReservationWaitingWithOrder> findByName(String name);
}
