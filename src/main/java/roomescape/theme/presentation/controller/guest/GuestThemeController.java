package roomescape.theme.presentation.controller.guest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.application.ThemeApplicationService;
import roomescape.theme.presentation.dto.response.ThemeWebResponse;

@RestController
public class GuestThemeController {

    private static final int POPULAR_THEMES_DAYS = 7;
    private static final int POPULAR_THEMES_LIMIT = 10;

    private final ThemeApplicationService themeApplicationService;

    public GuestThemeController(final ThemeApplicationService themeApplicationService) {
        this.themeApplicationService = themeApplicationService;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeWebResponse>> findAll() {
        return ResponseEntity.ok(themeApplicationService.findAll());
    }

    @GetMapping("/themes/popular")
    public ResponseEntity<List<ThemeWebResponse>> findPopular() {
        return ResponseEntity.ok(themeApplicationService.findPopular(POPULAR_THEMES_DAYS, POPULAR_THEMES_LIMIT));
    }
}
