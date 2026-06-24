package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findById(long id);

    Optional<ReservationSlot> findByIdWithLock(long id);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    ReservationSlot save(ReservationSlot reservationSlot);
}
