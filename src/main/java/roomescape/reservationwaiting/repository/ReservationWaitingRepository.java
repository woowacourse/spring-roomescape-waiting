package roomescape.reservationwaiting.repository;

import java.util.List;
import java.util.Map;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(Long id);

    Map<Long, Long> calculateTurn(String name);

    List<ReservationWaiting> findByName(String name);

    boolean existsByNameAndReservationId(String name, Long reservationId);

    ReservationWaiting findReservationWaitingById(Long reservationWaitingId);
}
