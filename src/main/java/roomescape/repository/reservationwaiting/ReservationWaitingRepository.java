package roomescape.repository.reservationwaiting;

import java.util.Optional;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    Optional<ReservationWaiting> findById(Long id);
    ReservationWaitingLine findLineBySlot(ReservationSlot slot);
    void delete(ReservationWaiting reservationWaiting);
}
