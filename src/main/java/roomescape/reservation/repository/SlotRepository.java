package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface SlotRepository {

    Slot findOrCreate(LocalDate date, ReservationTime time, Theme theme);

    void lockForUpdate(Long slotId);

    Optional<Slot> findById(Long id);
}
