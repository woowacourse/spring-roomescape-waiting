package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final ThemeDao themeDao;

    public ThemeRepositoryImpl(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    @Override
    public Theme save(Theme theme) {
        return themeDao.save(theme);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themeDao.findById(id);
    }

    @Override
    public boolean existsByName(Theme theme) {
        return themeDao.existsByName(theme.getName());
    }

    @Override
    public List<Theme> findAll() {
        return themeDao.findAll();
    }

    @Override
    public void delete(Theme theme) {
        themeDao.delete(theme);
    }
}
