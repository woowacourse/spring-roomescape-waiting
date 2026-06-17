package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ThemeSlot;

public interface JpaThemeSlotRepositoryCustom {

    Optional<ThemeSlot> findByIdForUpdate(long id);

    List<ThemeSlot> findAllByIdsForUpdateInOrder(Long firstId, Long secondId);
}
