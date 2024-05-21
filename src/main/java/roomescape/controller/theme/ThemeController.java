package roomescape.controller.theme;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.theme.Theme;
import roomescape.dto.theme.ThemeResponse;
import roomescape.dto.theme.ThemeSaveRequest;
import roomescape.service.ThemeService;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody final ThemeSaveRequest request) {
        final Theme theme = request.toModel();
        return ResponseEntity.status(HttpStatus.CREATED).body(themeService.create(theme));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        return ResponseEntity.ok(themeService.findAll());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> findAllPopular() {
        return ResponseEntity.ok(themeService.findPopularThemes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable final Long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
