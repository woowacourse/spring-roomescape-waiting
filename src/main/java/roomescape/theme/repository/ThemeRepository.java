package roomescape.theme.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

@Repository
public class ThemeRepository {

    private final ThemeDao themeDao;

    public ThemeRepository(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public Theme save(Theme theme) {
        return themeDao.save(theme);
    }

    public List<Theme> findAll() {
        return themeDao.findAllThemes();
    }

    public void deleteById(long themeId) {
        themeDao.deleteById(themeId);
    }
}
