package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.ThemeRequest;
import roomescape.controller.response.ReservationThemeResponse;
import roomescape.model.Theme;
import roomescape.service.ThemeService;

import java.net.URI;
import java.util.List;

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
    public ResponseEntity<ReservationThemeResponse> addTheme(@RequestBody ThemeRequest themeRequest) {
        Theme theme = themeService.addTheme(themeRequest);
        ReservationThemeResponse response = ReservationThemeResponse.of(theme);
        return ResponseEntity.created(URI.create("/themes/" + theme.getId())).body(response);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable(name = "id") long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/themes/top10")
    public ResponseEntity<List<ReservationThemeResponse>> getPopularThemes() {
        List<Theme> popularThemes = themeService.findPopularThemes();
        List<ReservationThemeResponse> responses = popularThemes.stream()
                .map(ReservationThemeResponse::of)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
