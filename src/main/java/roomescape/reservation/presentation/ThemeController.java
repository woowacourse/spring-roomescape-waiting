package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ThemeSaveRequest;
import roomescape.reservation.dto.response.ThemeResponse;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid ThemeSaveRequest request) {
        Theme newTheme = request.toModel();
        Theme createTheme = themeService.create(newTheme);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ThemeResponse.from(createTheme));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<Theme> themes = themeService.findAll();
        return ResponseEntity.ok(themes.stream()
                .map(ThemeResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> findAllPopular() {
        List<Theme> themes = themeService.findAllPopular();
        return ResponseEntity.ok(themes.stream()
                .map(ThemeResponse::from)
                .toList());
    }
}
