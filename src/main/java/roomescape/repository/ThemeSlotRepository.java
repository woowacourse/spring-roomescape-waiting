package roomescape.repository;

import roomescape.domain.ThemeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeSlotRepository {

    ThemeSlot save(ThemeSlot themeSlot);

    List<ThemeSlot> findByThemeIdAndDate(long themeId, LocalDate date);

    Optional<ThemeSlot> findById(long id);

    Optional<ThemeSlot> findByIdForUpdate(long id);

    void update(ThemeSlot themeSlot);
}
