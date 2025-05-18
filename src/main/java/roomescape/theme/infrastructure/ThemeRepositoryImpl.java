package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeCommandRepository;
import roomescape.theme.domain.ThemeQueryRepository;

@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeCommandRepository, ThemeQueryRepository {

    private static final int FIRST_PAGE = 0;

    private static final int MINIMUM_TOP_N_COUNT = 0;

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
    public Theme getByIdOrThrow(final Long id) {
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
            final int topN
    ) {
        if (topN <= MINIMUM_TOP_N_COUNT) {
            throw new IllegalArgumentException(String.format("인기 테마 조회 개수는 %d개 보다 많아야 합니다.", MINIMUM_TOP_N_COUNT));
        }
        final Pageable topThemePageRequest = PageRequest.of(FIRST_PAGE, topN);
        return jpaThemeRepository.findTopNThemesByReservationCountInDateRange(dateFrom, dateTo,
                topThemePageRequest);
    }
}
