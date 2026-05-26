package roomescape.controller;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.PopularThemeResponse;
import roomescape.controller.dto.ReservationTimeResponse;
import roomescape.controller.dto.ThemeResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/user/themes")
@Validated
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
            @PathVariable @Positive(message = "themeId는 0보다 커야합니다.") Long themeId,
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
