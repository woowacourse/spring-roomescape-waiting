package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.theme.ReservationReferencedThemeException;
import roomescape.service.dto.ThemeListResponse;
import roomescape.service.dto.ThemeRequest;
import roomescape.service.dto.ThemeResponse;

@Service
@Transactional
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ThemeListResponse findAllTheme() {
        List<Theme> themes = themeRepository.findAll();
        return new ThemeListResponse(themes.stream()
                .map(ThemeResponse::new)
                .toList());
    }

    @Transactional(readOnly = true)
    public ThemeListResponse findAllPopularTheme() {
        LocalDate startDate = LocalDate.now(clock).minusDays(7L);
        LocalDate endDate = LocalDate.now(clock);
        List<Theme> themes = reservationRepository.findThemeByMostPopularReservation(startDate, endDate);
        return new ThemeListResponse(themes.stream()
                .map(ThemeResponse::new)
                .toList());
    }

    public ThemeResponse saveTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    public void deleteTheme(long id) {
        Theme theme = findThemeById(id);
        if (reservationRepository.existsByThemeId(theme.getId())) {
            throw new ReservationReferencedThemeException();
        }
        themeRepository.delete(theme);
    }

    private Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
