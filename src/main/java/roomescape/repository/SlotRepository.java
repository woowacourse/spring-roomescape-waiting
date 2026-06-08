package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.domain.Slot;

public interface SlotRepository {

    Optional<Slot> findById(Long id);

    Optional<Slot> findByDateAndThemeAndTimeAndStore(LocalDate date, Long themeId, Long timeId, Long storeId);

    Slot save(Slot slot);
}