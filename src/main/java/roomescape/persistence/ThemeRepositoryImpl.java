package roomescape.persistence;

import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public ThemeRepositoryImpl(final JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    @Override
    public Theme save(final Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    public void deleteById(final Long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public List<Theme> findRankByDate(final LocalDate startDate, final LocalDate endDate, final int limit) {
        return jpaThemeRepository.findRankByDate(startDate, endDate, limit);
    }

}
