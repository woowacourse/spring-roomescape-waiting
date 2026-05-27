package roomescape.repository.reservationwaiting;

import roomescape.domain.reservationwaiting.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);
    void deleteByIdAndName(Long id, String name);
}
