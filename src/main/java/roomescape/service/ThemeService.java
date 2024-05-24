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
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.ThemeResponse;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public List<ThemeResponse> findAllTheme() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findAllPopularTheme() {
        LocalDate startDate = LocalDate.now(clock).minusDays(7L);
        LocalDate endDate = LocalDate.now(clock);
        List<Theme> themes = reservationRepository.findThemeByMostPopularReservation(startDate, endDate);
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional
    public ThemeResponse saveTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    @Transactional
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
