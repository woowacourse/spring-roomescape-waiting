package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.CurrentDateTime;
import roomescape.reservation.service.dto.ThemeCreateCommand;
import roomescape.reservation.service.dto.ThemeInfo;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ThemeDao;
import roomescape.reservation.domain.Theme;

@Service
public class ThemeService {
    private static final int POPULAR_THEME_FROM_DAYS_AGO = 7;
    private static final int POPULAR_THEME_TO_DAYS_AGO = 1;
    private static final int POPULAR_THEME_MAX_COUNT = 10;

    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;
    private final CurrentDateTime currentDateTime;

    public ThemeService(final ThemeDao themeDao, final ReservationDao reservationDao,
                        final CurrentDateTime currentDateTime) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
        this.currentDateTime = currentDateTime;
    }

    public ThemeInfo createTheme(final ThemeCreateCommand command) {
        Theme theme = command.convertToTheme();
        if (themeDao.isExists(theme.getThemeName())) {
            throw new IllegalArgumentException("해당 이름의 테마는 이미 존재합니다.");
        }
        final Theme savedTheme = themeDao.save(theme);
        return new ThemeInfo(savedTheme);
    }

    public List<ThemeInfo> findAll() {
        final List<Theme> themes = themeDao.findAll();
        return themes.stream()
                .map(ThemeInfo::new)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        if (reservationDao.isExistsByThemeId(id)) {
            throw new IllegalArgumentException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeDao.deleteById(id);
    }

    public List<ThemeInfo> findPopularThemes() {
        LocalDate today = currentDateTime.getDate();
        final LocalDate from = today.minusDays(POPULAR_THEME_FROM_DAYS_AGO);
        final LocalDate to = today.minusDays(POPULAR_THEME_TO_DAYS_AGO);
        final List<Theme> popularThemes = themeDao.findPopularThemes(from, to, POPULAR_THEME_MAX_COUNT);
        return popularThemes.stream()
                .map(ThemeInfo::new)
                .toList();
    }
}
