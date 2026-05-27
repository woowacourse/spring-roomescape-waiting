package roomescape.repository;

import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    boolean existBy(String name, Long reservationId);
}
