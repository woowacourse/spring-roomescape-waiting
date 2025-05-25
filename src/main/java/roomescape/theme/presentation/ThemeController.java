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
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.theme.application.ThemeApplicationService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeApplicationService themeApplicationService;

    public ThemeController(ThemeApplicationService themeApplicationService) {
        this.themeApplicationService = themeApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> responses = themeApplicationService.getThemes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/popular-themes")
    public ResponseEntity<List<PopularThemeResponse>> getPopularThemes() {
        List<PopularThemeResponse> responses = themeApplicationService.getPopularThemes();
        return ResponseEntity.ok().body(responses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody final ThemeRequest request) {
        ThemeResponse theme = themeApplicationService.createTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + theme.id())).body(theme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") final Long id) {
        themeApplicationService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }
}
