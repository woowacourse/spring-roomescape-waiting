package roomescape.repository;

import java.util.Optional;
import roomescape.domain.ReservationSlot;
import roomescape.repository.dto.ReservationCondition;

public interface ReservationSlotRepository {

    ReservationSlot save(ReservationSlot slot);

    Optional<ReservationSlot> findById(long id);

    Optional<ReservationSlot> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition);

    Optional<ReservationSlot> findByReservationIdForUpdate(long reservationId);

    void update(ReservationSlot slot);
}
