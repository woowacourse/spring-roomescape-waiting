package roomescape.repository.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.theme.jpa.ThemeJpaEntity;
import roomescape.repository.theme.jpa.ThemeJpaRepository;

@Repository
@Primary
public class JpaThemeRepositoryAdapter implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    public JpaThemeRepositoryAdapter(final ThemeJpaRepository themeJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
    }

    @Override
    public List<Theme> findAll() {
        return themeJpaRepository.findAll().stream()
                .map(ThemeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Theme> findById(final long id) {
        return themeJpaRepository.findById(id)
                .map(ThemeJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public int deleteById(final long id) {
        if (!themeJpaRepository.existsById(id)) {
            return 0;
        }

        try {
            themeJpaRepository.deleteById(id);
            themeJpaRepository.flush();
            return 1;
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    @Transactional
    public Theme save(final Theme theme) {
        try {
            ThemeJpaEntity saved = themeJpaRepository.saveAndFlush(ThemeJpaEntity.from(theme));
            return saved.toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    public boolean existsByName(final String name) {
        return themeJpaRepository.existsByName(name);
    }

    @Override
    public List<Theme> findPopularThemes(final int period, final int limit) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(period);

        return themeJpaRepository.findPopularThemes(start, end, limit).stream()
                .map(ThemeJpaEntity::toDomain)
                .toList();
    }
}
