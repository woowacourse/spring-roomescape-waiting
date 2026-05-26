package roomescape.repository;

import roomescape.domain.ThemeSlot;

import java.time.LocalDate;
import java.util.List;

public interface ThemeSlotRepository {

    ThemeSlot save(ThemeSlot themeSlot);

    List<ThemeSlot> saveAll(List<ThemeSlot> themeSlots);

    List<ThemeSlot> findByThemeIdAndDate(long themeId, LocalDate date);

    void deleteById(long id);

    boolean isExistBy(long themeId, LocalDate date);

    void update(ThemeSlot themeSlot);
}
