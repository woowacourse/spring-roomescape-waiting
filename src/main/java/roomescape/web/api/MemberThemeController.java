package roomescape.web.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.policy.WeeklyRankingPolicy;
import roomescape.service.ThemeService;
import roomescape.service.dto.response.theme.ThemeResponse;

@RestController
@RequiredArgsConstructor
public class MemberThemeController {
    private final ThemeService themeService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> findAllTheme() {
        List<ThemeResponse> response = themeService.findAllTheme();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/themes/ranking")
    public ResponseEntity<List<ThemeResponse>> findAllPopularTheme() {
        List<ThemeResponse> response = themeService.findAllPopularThemes(new WeeklyRankingPolicy());
        return ResponseEntity.ok().body(response);
    }
}
