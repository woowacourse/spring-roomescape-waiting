package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Theme;
import roomescape.service.dto.response.ThemeResponse;
import roomescape.service.theme.ThemeFindService;

import java.util.List;

@Validated
@RestController
public class ThemeApiController {

    private final ThemeFindService themeFindService;

    public ThemeApiController(ThemeFindService themeFindService) {
        this.themeFindService = themeFindService;
    }

    @GetMapping("/api/themes")
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<Theme> themes = themeFindService.findThemes();
        return ResponseEntity.ok(
                themes.stream()
                        .map(ThemeResponse::new)
                        .toList()
        );
    }

    @GetMapping("/api/themes/ranks")
    public ResponseEntity<List<ThemeResponse>> getThemeRanks() {
        List<Theme> themes = themeFindService.findThemeRanks();
        return ResponseEntity.ok(
                themes.stream()
                        .map(ThemeResponse::new)
                        .toList()
        );
    }
}
