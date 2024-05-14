package roomescape.theme.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.request.CreateThemeRequest;
import roomescape.theme.dto.response.CreateThemeResponse;
import roomescape.theme.dto.response.FindPopularThemesResponse;
import roomescape.theme.dto.response.FindThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<CreateThemeResponse> createTheme(@Valid @RequestBody CreateThemeRequest createThemeRequest) {
        CreateThemeResponse createThemeResponse = themeService.createTheme(createThemeRequest);
        return ResponseEntity.created(URI.create("/themes/" + createThemeResponse.id())).body(createThemeResponse);
    }

    @GetMapping
    public ResponseEntity<List<FindThemeResponse>> getThemes() {
         return ResponseEntity.ok(themeService.getThemes());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FindPopularThemesResponse>> getPopularThemes(@RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(themeService.getPopularThemes(size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
