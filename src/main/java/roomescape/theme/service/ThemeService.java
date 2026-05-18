package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;

@Service
public class ThemeService {

    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;

    public ThemeService(ReservationDao reservationDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
    }

    public List<Theme> findAll() {
        return themeDao.selectAll();
    }

    public Theme findById(Long id) {
        return themeDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<Theme> findByTrend(LocalDate startDate, LocalDate endDate, int limit) {
        return themeDao.selectTopThemesByReservation(startDate, endDate, limit);
    }

    public Theme add(String name, String description, String image) {
        Theme theme = new Theme(name, description, image);
        return themeDao.insert(theme);
    }

    public void removeById(Long id) {
        validateThemeExists(id);
        validateNotReservedTheme(id);
        themeDao.deleteById(id);
    }

    private void validateThemeExists(Long id) {
        themeDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateNotReservedTheme(Long themeId) {
        if (reservationDao.existsByThemeIdAndAfterDate(themeId, LocalDate.now())) {
            throw new RoomescapeException(ErrorCode.CANNOT_DELETE_RESERVED_THEME);
        }
    }
}
