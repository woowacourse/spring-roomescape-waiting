package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.CreateThemeRequest;
import roomescape.reservation.dto.response.CreateThemeResponse;
import roomescape.reservation.dto.response.FindPopularThemesResponse;
import roomescape.reservation.dto.response.FindThemeResponse;
import roomescape.reservation.service.ThemeService;

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
    public ResponseEntity<List<FindPopularThemesResponse>> getPopularThemes(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(themeService.getPopularThemes(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
