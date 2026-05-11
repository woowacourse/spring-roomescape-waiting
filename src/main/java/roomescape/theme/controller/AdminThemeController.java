package roomescape.theme.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@RestController
@RequestMapping("/admin")
public class AdminThemeController {
    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> create(@RequestBody AdminThemeRequest request) {
        Theme theme = themeService.add(
                request.name(),
                request.description(),
                request.image());
        ThemeResponse adminThemeResponse = ThemeResponse.from(theme);
        URI location = URI.create("/admin/themes" + adminThemeResponse.id());
        return ResponseEntity.created(location).body(adminThemeResponse);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        themeService.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
