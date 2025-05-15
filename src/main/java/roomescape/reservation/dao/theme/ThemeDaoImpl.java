package roomescape.reservation.dao.theme;

import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeDaoImpl implements ThemeDao {

    private final JpaThemeDao jpaThemeDao;

    public ThemeDaoImpl(JpaThemeDao jpaThemeDao) {
        this.jpaThemeDao = jpaThemeDao;
    }

    @Override
    public Theme save(Theme theme) {
        return jpaThemeDao.save(theme);
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeDao.findAll();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaThemeDao.findById(id);
    }

    @Override
    public int deleteById(Long id) {
        int deleteCount = jpaThemeDao.countById(id);
        if (deleteCount == 0) {
            return deleteCount;
        }
        jpaThemeDao.deleteById(id);
        return deleteCount;
    }

    @Override
    public boolean existsByName(String name) {
        return jpaThemeDao.existsByName(name);
    }
}
