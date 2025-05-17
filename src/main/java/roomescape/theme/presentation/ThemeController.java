package roomescape.theme.presentation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.request.ThemeRequest;
import roomescape.theme.dto.response.PopularThemeResponse;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping(ThemeController.THEME_BASE_URL)
public class ThemeController {

    public static final String THEME_BASE_URL = "/themes";
    private static final String SLASH = "/";

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> responses = themeService.getThemes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularThemeResponse>> getPopularThemes() {
        List<PopularThemeResponse> responses = themeService.getPopularThemes();
        return ResponseEntity.ok().body(responses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody final ThemeRequest request) {
        ThemeResponse theme = themeService.createTheme(request);
        return ResponseEntity.created(URI.create(THEME_BASE_URL + SLASH + theme.id())).body(theme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") final Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }
}
