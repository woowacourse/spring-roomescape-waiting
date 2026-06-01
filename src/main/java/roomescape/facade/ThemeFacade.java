package roomescape.facade;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ServiceThemeCreateRequest;
import roomescape.service.dto.response.ServiceThemeResponse;

@Service
public class ThemeFacade {

    private final ReservationService reservationService;
    private final ThemeService themeService;

    public ThemeFacade(ReservationService reservationService, ThemeService themeService) {
        this.reservationService = reservationService;
        this.themeService = themeService;
    }

    @Transactional
    public ServiceThemeResponse save(ServiceThemeCreateRequest request) {
        Theme themeWithoutId = request.toEntity();
        return ServiceThemeResponse.from(themeService.save(themeWithoutId));
    }

    public List<ServiceThemeResponse> findAll() {
        return themeService.findAll().stream()
                .map(ServiceThemeResponse::from)
                .toList();
    }

    public List<ServiceThemeResponse> findRanking(LocalDate startDate, LocalDate endDate) {
        return themeService.findRanking(startDate, endDate).stream()
                .map(ServiceThemeResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationService.validateReferencedTheme(id);
        themeService.delete(id);
    }
}
