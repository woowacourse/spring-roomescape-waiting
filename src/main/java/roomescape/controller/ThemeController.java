package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.response.PopularThemesResponse;
import roomescape.controller.dto.response.ThemeAvailableTimesResponse;
import roomescape.controller.dto.response.ThemesResponse;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;
import roomescape.service.dto.AvailableTimes;
import roomescape.service.dto.PopularTheme;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemesResponse> findThemes() {
        List<Theme> themes = themeService.findThemes();
        ThemesResponse response = ThemesResponse.from(themes);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/available-times")
    public ResponseEntity<ThemeAvailableTimesResponse> findAvailableTimes(@PathVariable Long id, @RequestParam LocalDate date) {
        AvailableTimes availableTimes = themeService.findAvailableTimes(id, date);
        ThemeAvailableTimesResponse response = ThemeAvailableTimesResponse.from(availableTimes);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularThemesResponse> findPopularThemes(@RequestParam int days, @RequestParam int limit) {
        List<PopularTheme> popularThemes = themeService.findPopularThemes(LocalDate.now(), days, limit);
        PopularThemesResponse response = PopularThemesResponse.from(popularThemes);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
