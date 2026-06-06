package roomescape.reservationwaiting.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.common.domain.ReservationSlot;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(Long id);

    Map<Long, Long> calculateTurn(String name);

    List<ReservationWaiting> findByName(String name);

    Optional<ReservationWaiting> findOldestBySlot(ReservationSlot slot);

    boolean isWaitingBy(ReservationSlot slot, String name);

    Optional<ReservationWaiting> findById(Long id);
}
