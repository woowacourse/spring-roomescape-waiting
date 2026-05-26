package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.AvailableReservationTimeResponse;
import roomescape.controller.dto.response.AvailableReservationTimesResponse;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.controller.dto.response.ThemesResponse;
import roomescape.service.ThemeService;

@RequestMapping("/themes")
@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemesResponse> getAllThemes() {
        List<ThemeResponse> responses = themeService.getAllThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(new ThemesResponse(responses));
    }

    @GetMapping("/popular")
    public ResponseEntity<ThemesResponse> getPopularThemes(
            @RequestParam(defaultValue = "10") int size) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today.minusDays(1);
        List<ThemeResponse> responses = themeService.getPopularThemes(size, startDate, endDate)
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(new ThemesResponse(responses));
    }

    @GetMapping("/{id}/times")
    public ResponseEntity<AvailableReservationTimesResponse> getReservationTimes(
            @PathVariable long id,
            @RequestParam LocalDate date) {
        List<AvailableReservationTimeResponse> responses = themeService.getAvailableTimes(id, date)
                .stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(new AvailableReservationTimesResponse(responses));
    }
}
