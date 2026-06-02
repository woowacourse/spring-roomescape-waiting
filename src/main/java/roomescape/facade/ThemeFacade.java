package roomescape.facade;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ServiceThemeCreateRequest;

@Service
@Transactional(readOnly = true)
public class ThemeFacade {

    private final ReservationService reservationService;
    private final ThemeService themeService;

    public ThemeFacade(ReservationService reservationService, ThemeService themeService) {
        this.reservationService = reservationService;
        this.themeService = themeService;
    }

    @Transactional
    public ThemeResponse save(ServiceThemeCreateRequest request) {
        Theme theme = themeService.save(request);
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> findAll() {
        return themeService.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findRanking(LocalDate startDate, LocalDate endDate) {
        return themeService.findRanking(startDate, endDate).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationService.validateReferencedTheme(id);
        themeService.delete(id);
    }
}
