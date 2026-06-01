package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> readThemes() {
        List<ThemeResponse> responses = themeService.getThemes();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/rankings")
    public ResponseEntity<List<ThemeResponse>> readThemeRankings() {
        List<ThemeResponse> responses = themeService.getThemeRankings();
        return ResponseEntity.ok(responses);
    }
}
