package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.ReservationRepository;
import roomescape.dao.ThemeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.VisitDate;
import roomescape.exception.ExistReservationException;
import roomescape.service.dto.input.ThemeInput;
import roomescape.service.dto.output.ThemeOutput;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.ExceptionDomainType.THEME;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeOutput createTheme(final ThemeInput input) {
        final Theme theme = themeRepository.save(Theme.of(null, input.name(), input.description(), input.thumbnail()));
        return ThemeOutput.toOutput(theme);
    }

    public List<ThemeOutput> getAllThemes() {
        final List<Theme> themes = themeRepository.findAll();
        return ThemeOutput.toOutputs(themes);
    }

    public List<ThemeOutput> getPopularThemes(final LocalDate date) {
        final VisitDate visitDate = new VisitDate(date);
        final List<Theme> themes = themeRepository.getPopularTheme(visitDate.beforeWeek().asString(), visitDate.beforeDay().asString(), 10);
        return ThemeOutput.toOutputs(themes);
    }

    public void deleteTheme(final long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ExistReservationException(THEME, id);
        }
        themeRepository.deleteById(id);
    }
}
