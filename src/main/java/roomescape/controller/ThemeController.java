package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.ThemeRequest;
import roomescape.controller.dto.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(convertToTimeResponses(themeService.allTheme()));
    }

    @GetMapping(params = {"topCount", "during"})
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam Long topCount,
            @RequestParam Long during
    ) {
        return ResponseEntity.ok(convertToTimeResponses(themeService.findPopularThemes(topCount, during)));
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTime(@Valid @RequestBody ThemeRequest themeRequest) {
        Theme theme = themeService.saveTheme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnailUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(theme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable long id) {
        themeService.removeTheme(id);
        return ResponseEntity.noContent().build();
    }

    private List<ThemeResponse> convertToTimeResponses(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
