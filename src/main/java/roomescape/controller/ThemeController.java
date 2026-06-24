package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.ThemeRequest;
import roomescape.controller.dto.ThemeResponse;
import roomescape.controller.dto.ThemeResponses;
import roomescape.domain.theme.Theme;
import roomescape.service.ThemeService;

@Validated
@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> getThemes() {
        return ResponseEntity.ok(ThemeResponses.from(themeService.findAllThemes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> getTheme(@PathVariable long id) {
        Theme theme = themeService.getThemeById(id);
        return ResponseEntity.ok(ThemeResponse.from(theme));
    }

    @GetMapping("/popular")
    public ResponseEntity<ThemeResponses> getPopularThemes(
            @RequestParam(value = "limit", defaultValue = "10") @Positive
            long limit,
            @RequestParam(value = "days", defaultValue = "7") @Positive
            long days
    ) {
        return ResponseEntity.ok(ThemeResponses.from(themeService.findPopularThemes(limit, days)));
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid ThemeRequest request) {
        Theme theme = themeService.saveTheme(request.name(), request.description(), request.thumbnailUrl(), request.price());
        return ResponseEntity.created(URI.create("/themes/" + theme.getId()))
                .body(ThemeResponse.from(theme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable long id) {
        themeService.removeTheme(id);
        return ResponseEntity.noContent().build();
    }
}
