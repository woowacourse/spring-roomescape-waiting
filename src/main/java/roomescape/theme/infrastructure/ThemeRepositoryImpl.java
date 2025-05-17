package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeCommandRepository;
import roomescape.theme.domain.ThemeQueryRepository;

@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeCommandRepository, ThemeQueryRepository {

    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public Long save(final Theme theme) {
        return jpaThemeRepository.save(theme).getId();
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
    public Theme getByIdOrThrow(Long id) {
        return jpaThemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
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
