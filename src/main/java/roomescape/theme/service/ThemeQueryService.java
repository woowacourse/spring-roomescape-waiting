package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
public class ThemeQueryService {

    private static final int POPULAR_THEME_COUNT = 10;
    private static final int POPULAR_THEME_RANGE_START_SUBTRACT = 8;
    private static final int POPULAR_THEME_RANGE_END_SUBTRACT = 1;

    private final ThemeRepository themeRepository;

    public ThemeQueryService(final ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme findById(final Long id) {
        return themeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("해당 테마를 찾을 수 없습니다."));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> getPopularThemes() {
        LocalDate now = LocalDate.now();

        LocalDate start = now.minusDays(POPULAR_THEME_RANGE_START_SUBTRACT);
        LocalDate end = now.minusDays(POPULAR_THEME_RANGE_END_SUBTRACT);

        return themeRepository.findPopularThemes(start, end, POPULAR_THEME_COUNT);
    }
}
