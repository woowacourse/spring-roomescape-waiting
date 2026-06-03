package roomescape.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.PopularThemeResponses;
import roomescape.dto.response.ThemeReservationTimeResponses;
import roomescape.dto.response.ThemeResponse;
import roomescape.dto.response.ThemeResponses;
import roomescape.service.ThemeService;

@Validated
@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> readThemes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(themeService.getThemes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> readTheme(@PathVariable Long id) {
        return ResponseEntity.ok(ThemeResponse.from(themeService.getTheme(id)));
    }

    @GetMapping("/{id}/times")
    public ResponseEntity<ThemeReservationTimeResponses> readThemeTimes(@PathVariable Long id,
                                                                        @RequestParam LocalDate date) {
        return ResponseEntity.ok(ThemeReservationTimeResponses.from(themeService.getThemeTimes(id, date)));
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularThemeResponses> readPopularThemes(
            @RequestParam @Min(1) @Max(100) Integer limit) {
        return ResponseEntity.ok(PopularThemeResponses.from(themeService.getPopularThemes(limit)));
    }
}
