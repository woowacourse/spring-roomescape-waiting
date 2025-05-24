package roomescape.controller.rest;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.request.CreateThemeRequest;
import roomescape.service.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeService.findAllThemes());
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid final CreateThemeRequest request) {
        final ThemeResponse response = themeService.createTheme(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable final Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> popularThemes() {
        final List<ThemeResponse> popularThemes = themeService.getWeeklyPopularThemes();
        return ResponseEntity.ok(popularThemes);
    }
}
