package roomescape.theme.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.theme.dto.ThemeRequest;
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
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse themeResponse = themeService.addTheme(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id()))
                .body(themeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findThemes() {
        return ResponseEntity.ok(themeService.findThemes());
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ThemeResponse>> findTrendingThemes(@RequestParam Long limit) {
        return ResponseEntity.ok(themeService.findTrendingThemes(limit).stream().toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
