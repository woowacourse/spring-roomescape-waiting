package roomescape.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import roomescape.domain.ThemeSlot;

public class JpaThemeSlotRepositoryImpl implements JpaThemeSlotRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ThemeSlot> findByIdForUpdate(long id) {
        ThemeSlot themeSlot = entityManager.find(ThemeSlot.class, id, LockModeType.PESSIMISTIC_WRITE);
        return Optional.ofNullable(themeSlot);
    }

    @Override
    public List<ThemeSlot> findAllByIdsForUpdateInOrder(Long firstId, Long secondId) {
        return Stream.of(firstId, secondId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .map(id -> entityManager.find(ThemeSlot.class, id, LockModeType.PESSIMISTIC_WRITE))
                .filter(Objects::nonNull)
                .toList();
    }
}
