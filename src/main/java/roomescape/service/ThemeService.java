package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;
import roomescape.dto.PopularThemeResult;
import roomescape.exception.theme.ThemeInUseException;
import roomescape.exception.theme.ThemeNotFoundException;

import java.time.LocalDate;
import java.util.List;

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
            int deleteCount = themeDao.delete(id);
            validateDeleted(deleteCount);
        } catch (DataIntegrityViolationException e) {
            throw new ThemeInUseException();
        }
    }

    private void validateDeleted(int deleteCount) {
        if (deleteCount == 0) {
            throw new ThemeNotFoundException();
        }
    }
}
