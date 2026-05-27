package roomescape.repository.reservationwaiting;

import roomescape.domain.reservationwaiting.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    int deleteByIdAndName(Long id, String name);
    boolean existsByReservationIdAndName(Long reservationId, String name);
}
