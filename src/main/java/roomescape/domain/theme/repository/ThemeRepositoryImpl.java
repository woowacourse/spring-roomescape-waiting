package roomescape.domain.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.domain.Theme;

@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public ThemeRepositoryImpl(JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public Theme save(Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public void deleteById(Long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    public List<Theme> findThemeOrderByReservationCount() {
        return jpaThemeRepository.findThemeOrderByReservationCount();
    }
}
