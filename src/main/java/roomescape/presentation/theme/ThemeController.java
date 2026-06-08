package roomescape.presentation.theme;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.theme.ThemeService;
import roomescape.application.theme.response.PopularThemesResponse;
import roomescape.application.theme.response.ThemesResponse;

@RestController
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/themes")
    public ResponseEntity<ThemesResponse> getAllTheme() {
        ThemesResponse response = themeService.getAllTheme();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/themes/rank")
    public ResponseEntity<PopularThemesResponse> getThemeRank() {
        PopularThemesResponse response = themeService.getThemeRank();
        return ResponseEntity.ok(response);
    }
}
