package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;

@Service
public class ThemeService {

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<Theme> findAll() {
        return themeDao.selectAll();
    }

    public Theme findById(Long id) {
        return themeDao.selectById(id);
    }

    public List<Theme> findByTrend(LocalDate startDate, LocalDate endDate, int limit) {
        return themeDao.selectByTrend(startDate, endDate, limit);
    }

    public Theme add(String name, String description, String image) {
        Theme theme = new Theme(name, description, image);
        return themeDao.insert(theme);
    }

    public void removeById(Long id) {
        themeDao.deleteById(id);
    }
}
