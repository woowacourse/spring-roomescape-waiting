package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.PopularThemeResult;
import roomescape.theme.dto.PopularThemeResponse;
import roomescape.theme.dto.ThemeResponse;

@RestController
@RequestMapping("/api/v1/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<Theme> themes = themeService.getThemes();
        List<ThemeResponse> themeResponses = ThemeResponse.fromAll(themes);
        return ResponseEntity.ok().body(themeResponses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularThemeResponse>> getPopularTheme(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {
        List<PopularThemeResult> popularThemeResults = themeService.getPopularThemes(from, to);
        List<PopularThemeResponse> popularThemeResponses = PopularThemeResponse.fromAll(popularThemeResults);
        return ResponseEntity.ok().body(popularThemeResponses);
    }
}
