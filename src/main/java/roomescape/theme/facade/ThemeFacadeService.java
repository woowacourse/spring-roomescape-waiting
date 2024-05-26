package roomescape.theme.facade;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRankResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@Service
public class ThemeFacadeService {

    private final ThemeService themeService;
    private final ReservationService reservationService;

    public ThemeFacadeService(ThemeService themeService, ReservationService reservationService) {
        this.themeService = themeService;
        this.reservationService = reservationService;
    }

    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        Theme theme = themeService.addTheme(themeRequest);

        return ThemeResponse.fromTheme(theme);
    }

    public List<ThemeRankResponse> findRankedThemes(LocalDate today) {
        List<Theme> themes = reservationService.findRankedThemes(today);

        return themes.stream()
                .map(ThemeRankResponse::fromTheme)
                .toList();
    }

    public List<ThemeResponse> findThemes() {
        List<Theme> foundTheme = themeService.findThemes();

        return foundTheme.stream()
                .map(ThemeResponse::fromTheme)
                .toList();
    }

    public void removeTheme(long themeId) {
        reservationService.validateBeforeRemoveTheme(themeId);

        themeService.removeTheme(themeId);
    }
}
