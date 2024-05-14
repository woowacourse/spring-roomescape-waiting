package roomescape.theme.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeCreateRequest themeCreateRequest) {
        ThemeResponse theme = themeService.createTheme(themeCreateRequest);
        return ResponseEntity.created(URI.create("/themes/" + theme.id()))
                .body(theme);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> readThemes() {
        List<ThemeResponse> themes = themeService.readThemes();
        return ResponseEntity.ok(themes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> readTheme(@PathVariable Long id) {
        ThemeResponse themeResponse = themeService.readTheme(id);
        return ResponseEntity.ok(themeResponse);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> readPopularThemes() {
        List<ThemeResponse> themeResponses = themeService.readPopularThemes();
        return ResponseEntity.ok(themeResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
