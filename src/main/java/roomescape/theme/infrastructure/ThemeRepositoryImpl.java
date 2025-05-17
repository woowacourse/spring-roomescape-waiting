package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeCommandRepository;
import roomescape.theme.domain.ThemeQueryRepository;

@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeCommandRepository, ThemeQueryRepository {

    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public Theme save(final Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public void deleteById(final Long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(final String name) {
        return jpaThemeRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Theme> findById(final Long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Theme> findTopNThemesByReservationCountInDateRange(
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final int limit
    ) {
        return jpaThemeRepository.findTopNThemesByReservationCountInDateRange(dateFrom, dateTo, limit);
    }
}
