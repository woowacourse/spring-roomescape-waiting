package roomescape.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.request.ThemeRequest;
import roomescape.model.Theme;
import roomescape.service.ThemeService;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<Theme>> getThemes() {
        return ResponseEntity.ok(themeService.findAllThemes());
    }

    @PostMapping("/themes")
    public ResponseEntity<Theme> addTheme(@RequestBody ThemeRequest themeRequest) {
        Theme theme = themeService.addTheme(themeRequest);
        return ResponseEntity
                .created(URI.create("/themes/" + theme.getId()))
                .body(theme);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable(name = "id") long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/themes/top10")
    public ResponseEntity<List<Theme>> getPopularThemes() {
        return ResponseEntity.ok(themeService.findPopularThemes());
    }
}
