package roomescape.controller.theme;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.theme.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam final int period,
            @RequestParam final int limit
    ) {
        return ResponseEntity.ok(
                themeService.getPopularThemes(period, limit).stream()
                        .map(ThemeResponse::from)
                        .toList()
        );
    }
}
