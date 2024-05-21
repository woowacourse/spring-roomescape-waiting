package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.PopularThemePeriod;

@Service
public class ThemeService {
    private static final int COUNT_OF_POPULAR_THEME = 10;

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> findThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findPopularThemes() {
        PopularThemePeriod popularThemePeriod = new PopularThemePeriod();
        LocalDate startDate = popularThemePeriod.getStartDate();
        LocalDate endDate = popularThemePeriod.getEndDate();

        return themeRepository.findThemesSortedByCountOfReservation(startDate, endDate, COUNT_OF_POPULAR_THEME)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse createTheme(ThemeCreateRequest request) {
        Theme theme = request.createTheme();
        validateExists(theme);
        return ThemeResponse.from(themeRepository.save(theme));
    }

    private void validateExists(Theme theme) {
        if (themeRepository.existsByName(new ThemeName(theme.getName()))) {
            throw new IllegalArgumentException("테마 이름은 중복될 수 없습니다.");
        }
    }

    public void deleteTheme(Long id) {
        themeRepository.deleteById(id);
    }
}
