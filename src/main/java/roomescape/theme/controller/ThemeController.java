package roomescape.theme.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.theme.dto.CreateThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody @Valid final CreateThemeRequest request) {
        final ThemeResponse response = themeService.createTheme(request);
        return ResponseEntity.created(URI.create("/thems/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        final List<ThemeResponse> responses = themeService.findAll();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> findPopularThemes() {
        List<ThemeResponse> responses = themeService.findPopularThemes();
        return ResponseEntity.ok().body(responses);
    }
}
