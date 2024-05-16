package roomescape.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

@Repository
public class ThemeRepository {
    private final ThemeDao themeDao;

    public ThemeRepository(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public Theme save(Theme theme) {
        return themeDao.save(theme);
    }

    public Themes findAll() {
        return new Themes(themeDao.findAll());
    }

    public Optional<Theme> findById(long id) {
        return themeDao.findById(id);
    }

    public void deleteById(long id) {
        themeDao.deleteById(id);
    }
}
