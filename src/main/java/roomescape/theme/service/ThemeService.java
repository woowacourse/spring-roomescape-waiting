package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.domain.PopularThemePeriod;

@Service
public class ThemeService {
    private static final int COUNT_OF_POPULAR_THEME = 10;

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<ThemeResponse> findThemes() {
        return themeDao.findThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findPopularThemes() {
        PopularThemePeriod popularThemePeriod = new PopularThemePeriod();
        LocalDate startDate = popularThemePeriod.getStartDate();
        LocalDate endDate = popularThemePeriod.getEndDate();

        return themeDao.findThemesSortedByCountOfReservation(startDate, endDate, COUNT_OF_POPULAR_THEME)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse createTheme(ThemeCreateRequest request) {
        Theme createdTheme = themeDao.createTheme(request.createTheme());
        return ThemeResponse.from(createdTheme);
    }

    public void deleteTheme(Long id) {
        themeDao.deleteTheme(id);
    }
}
