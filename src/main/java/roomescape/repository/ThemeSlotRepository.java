package roomescape.repository;

import roomescape.domain.ThemeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeSlotRepository {

    ThemeSlot save(ThemeSlot themeSlot);

    List<ThemeSlot> saveAll(List<ThemeSlot> themeSlots);

    List<ThemeSlot> findByThemeIdAndDate(long themeId, LocalDate date);

    Optional<ThemeSlot> findById(long id);

    void deleteById(long id);

    boolean isExistBy(long themeId, LocalDate date);

    void update(ThemeSlot themeSlot);
}
