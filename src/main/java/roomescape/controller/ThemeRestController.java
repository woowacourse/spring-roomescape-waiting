package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.ThemeService;

import java.net.URI;
import java.util.List;

@RestController
public class ThemeRestController {

    private final ThemeService themeService;

    public ThemeRestController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeResponse> create(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse newTheme = themeService.create(themeRequest);
        return ResponseEntity.created(URI.create("/admin/themes/" + newTheme.getId())).body(newTheme);
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> readAll() {
        return ResponseEntity.ok().body(themeService.findAll());
    }

    @GetMapping("/themes/popular")
    public ResponseEntity<List<ThemeResponse>> readPopularTheme() {
        return ResponseEntity.ok().body(themeService.findPopularTheme());
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
