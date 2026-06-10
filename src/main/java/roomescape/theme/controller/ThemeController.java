package roomescape.theme.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/api/themes")
@Validated
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> readThemes() {
        return ResponseEntity.ok(themeService.findAllThemes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> findById(
            @PathVariable @Positive(message = "테마 아이디는 1 이상이어야 합니다.") Long id) {
        return ResponseEntity.ok(themeService.findById(id));
    }

    @GetMapping("/popular-themes")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        List<ThemeResponse> popularThemes = themeService.getPopularThemes(1L, 10L);
        return ResponseEntity.ok(popularThemes);
    }
}
