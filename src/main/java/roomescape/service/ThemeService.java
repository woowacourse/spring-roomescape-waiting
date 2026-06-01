package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;
import roomescape.dto.PopularThemeResult;
import roomescape.exception.theme.ThemeInUseException;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    @Transactional
    public Theme createTheme(String name, String description, String imgUrl) {
        Long id = themeDao.insertTheme(name, description, imgUrl);
        return themeDao.findById(id);
    }

    public List<Theme> getThemes() {
        return themeDao.findAllThemes();
    }

    public List<PopularThemeResult> getPopularThemes(LocalDate from, LocalDate to) {
        return themeDao.findPopularThemes(from, to).stream()
                .map(PopularThemeResult::from)
                .toList();
    }

    @Transactional
    public void deleteTheme(Long id) {
        try {
            themeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ThemeInUseException();
        }
    }
}
