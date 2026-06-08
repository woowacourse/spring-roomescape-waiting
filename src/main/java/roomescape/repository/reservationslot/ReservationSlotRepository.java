package roomescape.repository.reservationslot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.theme.Theme;

public interface ReservationSlotRepository {

    List<ReservationSlot> findAll();

    Optional<ReservationSlot> findById(long slotId);

    Optional<ReservationSlot> findBySlot(ReservationSlot reservationSlot);

    List<ReservationSlot> findByDateAndTheme(LocalDate date, Theme theme);

    ReservationSlot save(ReservationSlot reservationSlot);
}
