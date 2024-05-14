package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.VisitDate;
import roomescape.exception.ExistReservationException;
import roomescape.exception.NotExistException;
import roomescape.service.dto.input.ThemeInput;
import roomescape.service.dto.output.ThemeOutput;

import static roomescape.exception.ExceptionDomainType.THEME;

@Service
public class ThemeService {

    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(final ThemeDao themeDao, final ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public ThemeOutput createTheme(final ThemeInput input) {
        final Theme theme = themeDao.create(Theme.of(null, input.name(), input.description(), input.thumbnail()));
        return ThemeOutput.toOutput(theme);
    }

    public List<ThemeOutput> getAllThemes() {
        final List<Theme> themes = themeDao.getAll();
        return ThemeOutput.toOutputs(themes);
    }

    public List<ThemeOutput> getPopularThemes(final LocalDate date) {
        final List<Theme> themes = themeDao.getPopularWeekendTheme(new VisitDate(date));
        return ThemeOutput.toOutputs(themes);
    }

    public void deleteTheme(final long id) {
        if (reservationDao.isExistByThemeId(id)) {
            throw new ExistReservationException(THEME, id);
        }
        if (themeDao.delete(id)) {
            return;
        }
        throw new NotExistException(THEME, id);
    }
}
