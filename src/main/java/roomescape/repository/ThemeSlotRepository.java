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

    List<ThemeSlot> findAllByIdsForUpdateInOrder(Long firstId, Long secondId);

    void updateReserved(ThemeSlot themeSlot);
}
