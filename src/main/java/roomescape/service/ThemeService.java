package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.ThemeDao;
import roomescape.entity.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.exception.custom.DuplicatedException;

@Service
public class ThemeService {

    public static final int TOP_RANK_PERIOD_DAYS = 7;
    public static final int TOP_RANK_THRESHOLD = 10;

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<Theme> findAllThemes() {
        return themeDao.findAllThemes();
    }

    public List<Theme> findTopReservedThemes() {
        LocalDate today = LocalDate.now();

        return themeDao.findTopReservedThemesInPeriodWithLimit(
            today.minusDays(TOP_RANK_PERIOD_DAYS), today, TOP_RANK_THRESHOLD);
    }

    public Theme addTheme(ThemeRequest request) {
        validateDuplicateTheme(request);

        return themeDao.addTheme(
            new Theme(request.name(), request.description(), request.thumbnail()));
    }

    private void validateDuplicateTheme(ThemeRequest request) {
        if (themeDao.existThemeByName(request.name())) {
            throw new DuplicatedException("theme");
        }
    }

    public void removeTheme(Long id) {
        themeDao.removeThemeById(id);
    }
}
