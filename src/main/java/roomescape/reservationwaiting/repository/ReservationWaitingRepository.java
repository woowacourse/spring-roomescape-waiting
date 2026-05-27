package roomescape.reservationwaiting.repository;

import java.util.List;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(Long id);

    List<ReservationWaitingTurnResponse> findByName(String name);

    boolean existsByNameAndReservationId(String name, Long reservationId);

    ReservationWaiting findReservationWaitingById(Long reservationWaitingId);
}
