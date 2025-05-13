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

    private final JdbcTemplateThemeRepository jdbcTemplateThemeRepository;
    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public boolean existsById(final ThemeId id) {
        return jdbcTemplateThemeRepository.existsById(id);
    }

    @Override
    public boolean existsByName(final ThemeName name) {
        return jdbcTemplateThemeRepository.existsByName(name);
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplateThemeRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(final ThemeId id) {
        return jdbcTemplateThemeRepository.findById(id);
    }

    @Override
    public Theme save(final Theme theme) {
        return jdbcTemplateThemeRepository.save(theme);
    }

    @Override
    public void deleteById(final ThemeId id) {
        jdbcTemplateThemeRepository.deleteById(id);
    }
}
