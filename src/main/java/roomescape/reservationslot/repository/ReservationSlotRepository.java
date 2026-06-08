package roomescape.reservationslot.repository;

import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationSlotRepository {

    ReservationSlot findOrCreate(LocalDate date, ReservationTime time, Theme theme);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, Long timeId, Long themeId);

    Optional<ReservationSlot> findByIdForUpdate(Long slotId);
}
