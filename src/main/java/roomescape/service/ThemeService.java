package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ThemeException;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_PERIOD_DAYS = 7;
    private static final int BASE_DATE_EXCLUDED_DAYS = 1;

    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;
    private final Clock clock;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao, Clock clock) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
        this.clock = clock;
    }

    public ThemeResponse create(ThemeRequest request) {
        validateUniqueTheme(request.name());
        Theme savedTheme = themeDao.save(request.toTheme());
        return ThemeResponse.from(savedTheme);
    }

    private void validateUniqueTheme(String name) {
        boolean exists = themeDao.existsByName(name);
        if (exists) {
            throw new ThemeException(ThemeErrorCode.THEME_ALREADY_EXISTS);
        }
    }

    public List<ThemeResponse> getThemes() {
        return themeDao.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> getThemeRankings() {
        LocalDate baseDate = LocalDate.now(clock);
        LocalDate startDate = baseDate.minusDays(POPULAR_THEME_PERIOD_DAYS);
        LocalDate endDate = baseDate.minusDays(BASE_DATE_EXCLUDED_DAYS);
        List<Theme> popularThemes = themeDao.findPopularThemesByPeriod(startDate, endDate);
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(long themeId) {
        validateReservationNotExistsBy(themeId);
        int affectedRows = themeDao.delete(themeId);

        if (affectedRows == 0) {
            throw new ThemeException(ThemeErrorCode.THEME_NOT_FOUND);
        }
    }

    private void validateReservationNotExistsBy(long themeId) {
        if (reservationDao.existsByTheme(themeId)) {
            throw new ThemeException(ThemeErrorCode.THEME_HAS_RESERVATION);
        }
    }
}
