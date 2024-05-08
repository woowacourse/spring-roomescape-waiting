package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.controller.request.ThemeRequest;
import roomescape.model.Theme;
import roomescape.repository.ThemeDao;

@Service
public class ThemeService {

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<Theme> findAllThemes() {
        return themeDao.findAllThemes();
    }

    public Theme addTheme(ThemeRequest themeRequest) {
        Theme theme = new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        return themeDao.addTheme(theme);
    }

    public void deleteTheme(long id) {
        themeDao.deleteTheme(id);
    }

    public List<Theme> findPopularThemes() {
        LocalDate before = LocalDate.now().minusDays(8);
        LocalDate after = LocalDate.now().minusDays(1);
        return themeDao.findThemeRankingByDate(before, after, 10);
    }
}
