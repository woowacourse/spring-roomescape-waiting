package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.service.ThemeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> responses = themeService.getAllThemes();

        return ResponseEntity.ok()
                .body(responses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.addTheme(request);
        URI location = URI.create("/themes/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteThemeById(id);

        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/rankings")
    public ResponseEntity<List<ThemeResponse>> getMostReservedThemes() {
        List<ThemeResponse> responses = themeService.getMostReservedThemes();

        return ResponseEntity.ok()
                .body(responses);
    }
}
