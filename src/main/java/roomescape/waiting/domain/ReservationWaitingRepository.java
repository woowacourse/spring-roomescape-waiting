package roomescape.waiting.domain;

import java.util.List;
import java.util.Optional;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.ReservationSlot;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    Optional<ReservationWaiting> findById(long id);

    List<ReservationWaiting> findAllByName(String name);

    void delete(ReservationWaiting reservationWaiting) throws NotFoundException;

    boolean hasWaitingAtSameTime(ReservationWaiting reservationWaiting);

    List<ReservationWaiting> queryAllBySlotForUpdate(ReservationSlot slot);

    List<ReservationWaiting> findAllBySlots(List<ReservationSlot> slots);
}
