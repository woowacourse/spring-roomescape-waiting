package roomescape.persistence.jdbc;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.persistence.ThemeRepository;
import roomescape.persistence.jdbc.dao.ThemeDao;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private final ThemeDao themeDao;

    @Override
    public boolean isActiveByName(String name) {
        return themeDao.isActiveByName(name);
    }

    @Override
    public Theme save(Theme theme) {
        return themeDao.save(theme);
    }

    @Override
    public void update(Theme theme) {
        themeDao.update(theme);
    }

    @Override
    public Optional<Theme> findById(long id) {
        return themeDao.findById(id);
    }
}
