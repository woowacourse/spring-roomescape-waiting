package roomescape.controller.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.findAllThemes();
        return ResponseEntity.ok().body(themes);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getTopThemes(
            @RequestParam("limit") long limit
    ) {
        List<ThemeResponse> topTheme = themeService.findTopTheme(limit);

        return ResponseEntity.ok().body(topTheme);
    }
}
