package roomescape.theme.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public boolean existsById(final ThemeId id) {
        return jpaThemeRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByName(final ThemeName name) {
        return jpaThemeRepository.existsByName(name);
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(final ThemeId id) {
        return jpaThemeRepository.findById(id.getValue());
    }

    @Override
    public Theme save(final Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public void deleteById(final ThemeId id) {
        jpaThemeRepository.deleteById(id.getValue());
    }
}
