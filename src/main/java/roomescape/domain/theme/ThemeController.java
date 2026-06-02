package roomescape.domain.theme;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.theme.dto.ThemeResponse;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes/top")
    public ResponseEntity<List<ThemeResponse>> getTopThemes() {
        List<ThemeResponse> responses = themeService.getTopThemes();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> responses = themeService.getAllThemes();
        return ResponseEntity.ok(responses);
    }
}
