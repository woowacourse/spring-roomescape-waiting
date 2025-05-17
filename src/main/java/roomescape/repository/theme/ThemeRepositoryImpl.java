package roomescape.repository.theme;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public ThemeRepositoryImpl(JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    @Override
    public long save(Theme theme) {
        return jpaThemeRepository.save(theme).getId();
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    public void deleteById(long id) {
        jpaThemeRepository.deleteById(id);
    }
}
