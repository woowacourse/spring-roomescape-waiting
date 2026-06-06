package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.domain.ReservationSlot;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findById(long id);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    ReservationSlot save(ReservationSlot reservationSlot);
}
