package roomescape.slot.infrastructure;

import roomescape.slot.Slot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotRepository {
    Slot save(Slot slot);

    Optional<Long> findSlotIdByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    Optional<Slot> findById(long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Slot> findAll();

    void deleteById(long id);

    boolean existsAlreadySlot(LocalDate date, long themeId, long timeId);
}
