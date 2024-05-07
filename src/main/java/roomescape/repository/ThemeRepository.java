package roomescape.repository;

import org.springframework.stereotype.Repository;
import roomescape.model.theme.Theme;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ThemeDao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class ThemeRepository {

    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;

    public ThemeRepository(ReservationDao reservationDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
    }

    public List<Theme> findAllThemes() {
        return themeDao.findAll();
    }

    public Optional<Theme> saveTheme(Theme theme) {
        long id = themeDao.save(theme);
        return themeDao.findById(id);
    }

    public void deleteThemeById(long id) {
        themeDao.deleteById(id);
    }

    public List<Theme> findThemeRankingByDate(LocalDate startDate, LocalDate endDate, int rankingCount) {
        List<Theme> result = new ArrayList<>();
        List<Long> themeIds = reservationDao.findThemeIdByDateAndOrderByThemeIdCountAndLimit(startDate, endDate, rankingCount);
        for (long themeId : themeIds) {
            Theme theme = themeDao.findById(themeId).orElseThrow(NoSuchElementException::new);
            result.add(theme);
        }
        return result;
    }

    public boolean isExistThemeById(long id) {
        return themeDao.isExistById(id);
    }

    public boolean isExistThemeByName(String name) {
        return themeDao.isExistByName(name);
    }
}
