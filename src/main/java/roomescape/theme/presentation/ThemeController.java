package roomescape.theme.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeAddRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<Theme>> getThemeList() {
        return ResponseEntity.ok(themeService.findAllTheme());
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> addTheme(@Valid @RequestBody ThemeAddRequest themeAddRequest) {
        ThemeResponse saveResponse = themeService.saveTheme(themeAddRequest);
        URI createdUri = URI.create("/themes/" + saveResponse.id());
        return ResponseEntity.created(createdUri).body(saveResponse);
    }

    @GetMapping("/themes/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularTheme() {
        return ResponseEntity.ok(themeService.findPopularTheme());
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") Long id) {
        themeService.removeTheme(id);
        return ResponseEntity.noContent().build();
    }
}
