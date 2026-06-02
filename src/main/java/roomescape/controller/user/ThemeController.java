package roomescape.controller.user;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.PopularThemeResponse;
import roomescape.controller.dto.ThemeResponse;
import roomescape.controller.dto.TimeAvailabilityResponse;
import roomescape.service.ReservationAvailabilityService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ThemeController(ThemeService themeService, ReservationAvailabilityService reservationAvailabilityService) {
        this.themeService = themeService;
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themes = themeService.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themes);
    }

    @GetMapping("/{id}/times")
    public ResponseEntity<List<TimeAvailabilityResponse>> getAvailableTimes(
            @PathVariable @Positive(message = "id는 양수이어야 합니다.") Long id, @RequestParam("date") LocalDate date) {
        List<TimeAvailabilityResponse> times = reservationAvailabilityService.findAvailableTimes(id, date).stream()
                .map(TimeAvailabilityResponse::from)
                .toList();
        return ResponseEntity.ok(times);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularThemeResponse>> getPopularThemes() {
        List<PopularThemeResponse> themes = themeService.findWeeklyTopTen(LocalDate.now()).stream()
                .map(PopularThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themes);
    }
}
