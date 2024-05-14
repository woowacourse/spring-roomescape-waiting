package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.repository.jpa.JpaThemeDao;

@Repository
public class JpaThemeRepository implements ThemeRepository {
    private final JpaThemeDao jpaThemeDao;

    public JpaThemeRepository(JpaThemeDao jpaThemeDao) {
        this.jpaThemeDao = jpaThemeDao;
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeDao.findAll();
    }

    @Override
    public List<Theme> findAndOrderByPopularity(LocalDate start, LocalDate end, int count) {
        return jpaThemeDao.findAndOrderByPopularity(start, end, count);
    }

    @Override
    public Optional<Theme> findById(long id) {
        return jpaThemeDao.findById(id);
    }

    @Override
    public Theme save(Theme theme) {
        return jpaThemeDao.save(theme);
    }

    @Override
    public void delete(long id) {
        jpaThemeDao.deleteById(id);
    }
}
