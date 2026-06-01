package roomescape.repository.reservationwaiting;

import java.util.Optional;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    Optional<ReservationWaiting> findById(Long id);
    int deleteById(Long id);
    boolean existsByReservationIdAndName(Long reservationId, String name);
}
