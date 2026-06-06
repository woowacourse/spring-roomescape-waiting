package roomescape.slot.repository;

import roomescape.slot.domain.ReservationSlot;

import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository {

    ReservationSlot save(ReservationSlot slot);

    List<ReservationSlot> findAll();

    Optional<ReservationSlot> findById(Long slotId);

    Optional<ReservationSlot> findAvailableByDateIdTimeIdThemeId(Long dateId, Long timeId, Long themeId);

    Optional<ReservationSlot> findAvailableByDateIdTimeIdThemeIdForUpdate(Long dateId, Long timeId, Long themeId);
}
