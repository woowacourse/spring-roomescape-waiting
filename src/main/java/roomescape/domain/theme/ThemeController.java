package roomescape.domain.theme;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.theme.dto.AdminThemeResponse;
import roomescape.domain.theme.dto.ThemeCreationRequest;
import roomescape.domain.theme.dto.ThemeCreationResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeResponse;

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
