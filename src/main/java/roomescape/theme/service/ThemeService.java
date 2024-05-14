package roomescape.theme.service;

import org.springframework.stereotype.Service;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.dto.ThemesResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeDao themeDao;

    public ThemeService(final ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public ThemesResponse findAllThemes() {
        List<ThemeResponse> response = themeDao.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ThemesResponse(response);
    }

    public ThemesResponse getTop10Themes(final LocalDate today) {
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today.minusDays(1);
        int limit = 10;

        List<ThemeResponse> response = themeDao.findByStartDateAndEndDateWithLimit(startDate, endDate, limit)
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ThemesResponse(response);
    }

    public ThemeResponse addTheme(final ThemeRequest request) {
        Theme theme = themeDao.insert(new Theme(request.name(), request.description(), request.thumbnail()));

        return ThemeResponse.from(theme);
    }

    public void removeThemeById(final Long id) {
        themeDao.deleteById(id);
    }
}
