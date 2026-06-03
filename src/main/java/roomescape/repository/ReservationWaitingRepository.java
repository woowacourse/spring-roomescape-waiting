package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.WaitingWithOrder;

public interface ReservationWaitingRepository {

    WaitingWithOrder save(ReservationWaiting reservationWaiting);

    boolean existBy(String name, Long reservationId);

    Optional<ReservationWaiting> findById(Long id);

    Optional<ReservationWaiting> findEarliestByReservationId(Long reservationId);

    List<WaitingWithOrder> findByName(String name);

    void deleteById(Long id);
}
