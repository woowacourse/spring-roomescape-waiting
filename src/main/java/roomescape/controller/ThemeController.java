package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.ThemePatchRequest;
import roomescape.controller.dto.ThemeRequest;
import roomescape.controller.dto.ThemeResponse;
import roomescape.domain.Theme;
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
    public ResponseEntity<List<ThemeResponse>> themes() {
        return ResponseEntity.ok(convertToThemeResponses(themeService.allTheme()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> getTheme(@PathVariable long id) {
        Theme theme = themeService.findThemeById(id);
        return ResponseEntity.ok(ThemeResponse.from(theme));
    }

    @GetMapping(params = {"topCount", "during"})
    public ResponseEntity<List<ThemeResponse>> popularThemes(
            @RequestParam("topCount") Long topCount,
            @RequestParam("during") Long during
    ) {
        return ResponseEntity.ok(convertToThemeResponses(themeService.findPopularThemes(topCount, during)));
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid ThemeRequest request) {
        Theme theme = themeService.saveTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + theme.getId()))
                .body(ThemeResponse.from(theme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable long id) {
        themeService.removeTheme(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThemeResponse> updateTheme(
            @PathVariable
            long id,
            @RequestBody @Valid
            ThemeRequest request
    ) {
        return ResponseEntity.ok(ThemeResponse.from(themeService.putTheme(id, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ThemeResponse> patchTheme(
            @PathVariable long id,
            @RequestBody ThemePatchRequest request
    ) {
        return ResponseEntity.ok(ThemeResponse.from(themeService.patchTheme(id, request)));
    }

    private List<ThemeResponse> convertToThemeResponses(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
