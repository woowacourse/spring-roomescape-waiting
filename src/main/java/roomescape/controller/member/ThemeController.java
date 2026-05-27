package roomescape.controller.member;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ReservationTimeDetailResponse;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.dto.result.ReservationTimeDetailResult;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<ReservationTimeDetailResponse>> getThemeSchedule(
            @PathVariable long id,
            @RequestParam LocalDate date
    ) {
        List<ReservationTimeDetailResult> availableTimes = themeService.findThemeSchedule(id, date);
        List<ReservationTimeDetailResponse> response = availableTimes.stream()
                .map(ReservationTimeDetailResponse::from)
                .toList();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.findAllThemes().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok().body(themes);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getTopThemes(
            @RequestParam("limit") long limit
    ) {
        List<ThemeResponse> topTheme = themeService.findTopTheme(limit).stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok().body(topTheme);
    }
}