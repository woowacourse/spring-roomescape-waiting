package roomescape.theme.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.theme.application.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @Auth(Role.USER)
    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes(
    ) {
        return ResponseEntity.ok().body(
                themeService.getThemes()
        );
    }

    @Auth(Role.GUEST)
    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
    ) {
        return ResponseEntity.ok().body(
                themeService.getPopularThemes()
        );
    }
}
