package roomescape.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

@Repository
public class ThemeRepositoryAdaptor implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public ThemeRepositoryAdaptor(JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    @Override
    public Theme save(Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public void deleteById(long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public Optional<Theme> findByName(String name) {
        return jpaThemeRepository.findByName(name);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaThemeRepository.findById(id);
    }
}
