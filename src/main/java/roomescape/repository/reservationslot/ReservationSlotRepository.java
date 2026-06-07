package roomescape.repository.reservationslot;

import java.util.List;
import java.util.Optional;
import roomescape.domain.reservationslot.ReservationSlot;

public interface ReservationSlotRepository {



    List<ReservationSlot> findAll();

    Optional<ReservationSlot> findById(long slotId);

    Optional<ReservationSlot> findBySlot(ReservationSlot reservationSlot);

    ReservationSlot save(ReservationSlot reservationSlot);

}
