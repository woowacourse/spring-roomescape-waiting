package roomescape.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.service.ThemeService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public List<ThemeResponse> getThemes() {
        return themeService.getThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping("/popular-themes")
    public List<ThemeResponse> getPopularThemes() {
        return themeService.getPopularThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping("/available-reservation-times")
    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(@RequestParam("date") final LocalDate date, @RequestParam("theme-id") final Long themeId) {
        return themeService.getAvailableReservationTimes(date, themeId)
                .values()
                .entrySet()
                .stream()
                .map(entry -> AvailableReservationTimeResponse.of(entry.getKey(), entry.getValue()))
                .toList();
    }
}
