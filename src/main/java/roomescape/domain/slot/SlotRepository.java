package roomescape.domain.slot;

import java.time.LocalDate;
import java.util.Optional;

public interface SlotRepository {

    Optional<Slot> findByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId);

    boolean isExistByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId);

    Optional<Slot> findById(Long id);

    Long insert(Slot slot);

    long delete(Long id);
}
