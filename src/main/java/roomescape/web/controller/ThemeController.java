package roomescape.web.controller;

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
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody ThemeRequest themeRequest) {
        Long savedId = themeService.addTheme(themeRequest);
        ThemeResponse themeResponse = themeService.getTheme(savedId);
        return ResponseEntity.created(URI.create("/themes/" + savedId)).body(themeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllReservations() {
        List<ThemeResponse> themeResponses = themeService.getAllTheme();
        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> getReservation(@PathVariable Long id) {
        ThemeResponse themeResponses = themeService.getTheme(id);
        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularTheme() {
        return ResponseEntity.ok(themeService.getPopularThemes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
