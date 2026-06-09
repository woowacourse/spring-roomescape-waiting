package roomescape.repository.reservationwaiting;

import java.util.Optional;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    int deleteByIdAndName(Long id, String name);
    int deleteById(long id);
    boolean existsByReservationIdAndName(Long reservationId, String name);
    boolean existsByReservationId(Long reservationId);
    Optional<ReservationWaiting> findEarliestByReservationId(long reservationId);
}
