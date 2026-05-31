package roomescape.repository;

import java.util.Optional;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    boolean existBy(String name, Long reservationId);

    Optional<ReservationWaiting> findById(Long id);

    void deleteById(Long id);
}
