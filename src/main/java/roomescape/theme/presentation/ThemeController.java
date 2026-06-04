package roomescape.theme.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.application.ThemeService;
import roomescape.theme.presentation.response.ThemeRankResponse;
import roomescape.theme.presentation.response.ThemeResponse;

@RestController
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getAllTheme() {
        List<ThemeResponse> response = themeService.getAllTheme();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/themes/rank")
    public ResponseEntity<List<ThemeRankResponse>> getThemeRank() {
        List<ThemeRankResponse> response = themeService.getThemeRank();
        return ResponseEntity.ok(response);
    }
}
