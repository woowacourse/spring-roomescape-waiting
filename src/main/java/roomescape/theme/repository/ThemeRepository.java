package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ConflictException;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeErrorCode;

@Repository
public class ThemeRepository {

    private final ThemeDao themeDao;

    public ThemeRepository(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public Theme save(Theme theme) {
        try {
            return themeDao.save(theme);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ThemeErrorCode.DUPLICATE_THEME);
        }
    }

    public Optional<Theme> findById(long id) {
        return themeDao.findById(id);
    }

    public List<Theme> findAll() {
        return themeDao.findAll();
    }

    public void delete(Theme theme) {
        try {
            themeDao.delete(theme);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ThemeErrorCode.THEME_IN_USE);
        }
    }
}
