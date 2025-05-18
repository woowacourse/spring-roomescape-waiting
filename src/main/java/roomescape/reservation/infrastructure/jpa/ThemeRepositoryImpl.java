package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeName;
import roomescape.reservation.domain.theme.ThemeRepository;

@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    public ThemeRepositoryImpl(final ThemeJpaRepository themeJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
    }

    @Override
    public boolean existsByThemeName(final ThemeName themeName) {
        return themeJpaRepository.existsByThemeName(themeName);
    }

    @Override
    public Theme save(final Theme theme) {
        return themeJpaRepository.save(theme);
    }

    @Override
    public void deleteById(final long id) {
        themeJpaRepository.deleteById(id);
    }

    @Override
    public List<Theme> findAll() {
        return themeJpaRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(final long id) {
        return themeJpaRepository.findById(id);
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate from, final LocalDate to, final int count) {
        return themeJpaRepository.findPopularThemes(from, to, count);
    }
}
