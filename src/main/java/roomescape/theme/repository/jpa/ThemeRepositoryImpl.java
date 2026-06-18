package roomescape.theme.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeInUseException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.repository.entity.ThemeEntity;

@Profile("jpa")
@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    @Override
    public List<Theme> findAll() {
        return themeJpaRepository.findAll().stream()
            .map(ThemeEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Theme> findById(final Long themeId) {
        return themeJpaRepository.findById(themeId).map(ThemeEntity::toDomain);
    }

    @Override
    public Theme save(final Theme theme) {
        return themeJpaRepository.saveAndFlush(ThemeEntity.from(theme)).toDomain();
    }

    @Override
    public boolean deleteById(final Long themeId) {
        if (!themeJpaRepository.existsById(themeId)) {
            return false;
        }
        try {
            themeJpaRepository.deleteById(themeId);
            themeJpaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ThemeInUseException(e);
        }
        return true;
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate startDate, final LocalDate today) {
        return themeJpaRepository.findPopularThemes(startDate, today).stream()
            .map(ThemeEntity::toDomain)
            .toList();
    }
}
