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

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<ReservationTimeDetailResponse>> getThemeSchedulesByDate(
            @PathVariable long id,
            @RequestParam LocalDate date
    ) {
        List<ReservationTimeDetailResult> availableTimes = themeService.findThemeSchedulesByDate(id, date);
        List<ReservationTimeDetailResponse> response = availableTimes.stream()
                .map(ReservationTimeDetailResponse::from)
                .toList();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themes = themeService.findThemes().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok().body(themes);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam("limit") int limit
    ) {
        List<ThemeResponse> popularThemes = themeService.findPopularThemes(limit).stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok().body(popularThemes);
    }
}
