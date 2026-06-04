package roomescape.repository.reservationwaiting;

import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    Optional<ReservationWaiting> findById(Long id);
    ReservationWaitingLine findLineByReservation(Reservation reservation);
    void delete(ReservationWaiting reservationWaiting);
}
