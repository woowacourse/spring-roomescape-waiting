package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
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

    public Optional<Theme> findById(long id) {
        return themeDao.findById(id);
    }

    public List<Theme> findAll() {
        return themeDao.findAll();
    }

    public void delete(Theme theme) {
        themeDao.delete(theme);
    }
}
