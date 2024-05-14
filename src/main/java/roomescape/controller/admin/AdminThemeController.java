package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.ThemeResponse;
import roomescape.service.dto.ThemeSaveRequest;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> saveTheme(@RequestBody @Valid ThemeSaveRequest themeSaveRequest) {
        ThemeResponse themeResponse = themeService.saveTheme(themeSaveRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id()))
                .body(themeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themeResponses = themeService.getThemes();
        return ResponseEntity.ok(themeResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
