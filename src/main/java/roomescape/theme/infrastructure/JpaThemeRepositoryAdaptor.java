package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
public class JpaThemeRepositoryAdaptor implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public JpaThemeRepositoryAdaptor(JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    @Override
    public Theme save(Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaThemeRepository.existsById(id);
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate start, LocalDate end, int limit) {
        return jpaThemeRepository.findPopularThemes(start, end, limit);
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }
}
