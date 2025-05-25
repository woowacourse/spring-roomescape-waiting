package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public Theme save(final Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public void deleteById(final Long themeId) {
        jpaThemeRepository.deleteById(themeId);
    }

    @Override
    public boolean existsByName(final String name) {
        return jpaThemeRepository.existsByName(name);
    }

    @Override
    public Theme getById(final Long themeId) {
        return jpaThemeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다. id = " + themeId));
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public List<Theme> findTopNThemesByReservationCountInDateRange(
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final int limit
    ) {
        return jpaThemeRepository.findTopNThemesByReservationCountInDateRange(dateFrom, dateTo, limit);
    }
}
