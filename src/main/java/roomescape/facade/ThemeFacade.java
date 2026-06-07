package roomescape.facade;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.response.ThemeListResponse;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;

@Service
public class ThemeFacade {

    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final WaitService waitService;

    public ThemeFacade(ReservationService reservationService, ThemeService themeService, WaitService waitService) {
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.waitService = waitService;
    }

    @Transactional
    public ThemeResponse save(ThemeCreateRequest request) {
        Theme themeWithoutId = request.toEntity();
        return ThemeResponse.from(themeService.save(themeWithoutId));
    }

    public ThemeListResponse findAll() {
        return ThemeListResponse.from(themeService.findAll());
    }

    public ThemeListResponse findRanking(LocalDate startDate, LocalDate endDate) {
        return ThemeListResponse.from(themeService.findRanking(startDate, endDate));
    }

    @Transactional
    public void delete(Long id) {
        reservationService.validateReferencedTheme(id);
        waitService.validateReferencedTheme(id);
        themeService.delete(id);
    }
}
