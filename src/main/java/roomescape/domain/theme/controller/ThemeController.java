package roomescape.domain.theme.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddRequest;
import roomescape.domain.theme.service.ThemeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

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
    public ResponseEntity<Theme> addTheme(@RequestBody ThemeAddRequest themeAddRequest) {
        Theme theme = themeService.addTheme(themeAddRequest);
        return ResponseEntity.created(URI.create("/themes" + theme.getId())).body(theme);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") Long id) {
        themeService.removeTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/theme/ranking")
    public ResponseEntity<List<Theme>> getThemeRank() {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(themeService.getThemeRanking(now));
    }
}
