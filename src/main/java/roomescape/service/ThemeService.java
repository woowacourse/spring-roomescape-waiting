package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;
import roomescape.dto.command.ThemeCommand;
import roomescape.dto.response.ThemeResponse;

@Service
@Transactional
public class ThemeService {
    private static final int POPULAR_THEME_PERIOD_DAYS = 6;

    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public ThemeResponse addTheme(ThemeCommand command) {
        validateUniqueTheme(command.name());

        Theme theme = Theme.createWithoutId(command.name(), command.description(), command.thumbnail());
        Theme savedTheme = themeDao.insert(theme);
        return ThemeResponse.from(savedTheme);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getThemes() {
        return themeDao.selectAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getPopularThemes(LocalDate today) {
        LocalDate startDate = today.minusDays(POPULAR_THEME_PERIOD_DAYS);
        LocalDate endDate = today.minusDays(1);

        List<Theme> popularThemes = themeDao.selectPopularThemesByPeriod(startDate, endDate);
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteTheme(long themeId) {
        Optional<Theme> theme = themeDao.selectById(themeId);
        if (theme.isEmpty()) {
            throw new RoomEscapeException(ThemeErrorCode.NOT_FOUND);
        }

        validateThemeIncludeReservation(themeId);
        themeDao.delete(themeId);
    }

    private void validateUniqueTheme(String name) {
        boolean exists = themeDao.existsByName(name);
        if (exists) {
            throw new RoomEscapeException(ThemeErrorCode.DUPLICATE);
        }
    }

    private void validateThemeIncludeReservation(long themeId) {
        boolean existsByThemeId = reservationDao.existsByThemeId(themeId);
        if (existsByThemeId) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_CANNOT_DELETE);
        }
    }
}
