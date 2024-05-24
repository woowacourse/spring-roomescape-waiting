package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
import roomescape.controller.request.ThemeRequest;
import roomescape.controller.response.ThemeResponse;
import roomescape.model.theme.Theme;
import roomescape.service.ThemeService;
import roomescape.service.dto.ThemeDto;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<Theme> themes = themeService.findAllThemes();
        List<ThemeResponse> response = themes.stream()
                .map(ThemeResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@Valid @RequestBody ThemeRequest request) {
        ThemeDto themeDto = ThemeDto.from(request);
        Theme theme = themeService.saveTheme(themeDto);
        ThemeResponse response = new ThemeResponse(theme);
        return ResponseEntity
                .created(URI.create("/themes/" + response.getId()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@Min(1) @PathVariable("id") Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        List<Theme> themes = themeService.findPopularThemes();
        List<ThemeResponse> response = themes.stream()
                .map(ThemeResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }
}
