package roomescape.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.ThemeRequest;
import roomescape.controller.dto.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themes = themeService.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themes);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest request) {
        Theme theme = themeService.create(
                request.name(),
                request.description(),
                request.thumbnail());
        return ResponseEntity.created(URI.create("/admin/themes/" + theme.getId()))
                .body(ThemeResponse.from(theme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable @Positive(message = "id는 양수이어야 합니다.") Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
