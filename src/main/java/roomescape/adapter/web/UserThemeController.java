package roomescape.adapter.web;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.adapter.web.dto.response.PopularThemeResponse;
import roomescape.adapter.web.dto.response.ReservationTimeResponse;
import roomescape.adapter.web.dto.response.ThemeResponse;
import roomescape.application.ReservationTimeService;
import roomescape.application.ThemeService;

@RestController
@RequestMapping("/user/themes")
public class UserThemeController {

    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public UserThemeController(
            ThemeService themeService,
            ReservationTimeService reservationTimeService
    ) {
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public List<ThemeResponse> list() {
        return themeService.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping("/{themeId}/available-times")
    public List<ReservationTimeResponse> availableTimes(
            @PathVariable Long themeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reservationTimeService.findAvailable(date, themeId).stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @GetMapping("/popular")
    public List<PopularThemeResponse> popular() {
        return themeService.findPopular().stream()
                .map(PopularThemeResponse::from)
                .toList();
    }

}
