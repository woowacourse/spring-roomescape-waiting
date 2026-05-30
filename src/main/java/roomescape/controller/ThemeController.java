package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.ThemePatchRequest;
import roomescape.controller.dto.ThemeRequest;
import roomescape.controller.dto.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;

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
        Theme theme = themeService.saveTheme(request.name(), request.description(), request.thumbnailUrl());
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
            @PathVariable long id,
            @RequestBody @Valid ThemeRequest request
    ) {
        themeService.putTheme(id, request.name(), request.description(), request.thumbnailUrl());
        return ResponseEntity.ok(ThemeResponse.from(themeService.findThemeById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ThemeResponse> patchTheme(
            @PathVariable long id,
            @RequestBody ThemePatchRequest request
    ) {
        themeService.patchTheme(id, request.name(), request.description(), request.thumbnailUrl());
        return ResponseEntity.ok(ThemeResponse.from(themeService.findThemeById(id)));
    }

    private List<ThemeResponse> convertToThemeResponses(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
