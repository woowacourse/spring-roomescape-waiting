package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.ThemeResponse;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public ThemeResponse createTheme(ThemeRequest themeRequest) {
        Theme theme = themeRepository.save(themeRequest.toEntity());
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeRepository.findAllThemes();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findWeeklyTop10Themes() {
        return findLimitThemes(10);
    }

    private List<ThemeResponse> findLimitThemes(int limit) {
        LocalDate now = LocalDate.now(clock);

        LocalDate start = now.minusDays(8);
        LocalDate end = now.minusDays(1);
        return themeRepository.findTopReservedThemesByDateRangeAndLimit(start, end, limit).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteTheme(Long id) {
        themeRepository.delete(id);
    }
}
