package roomescape.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.PopularThemeRequest;
import roomescape.service.dto.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themeResponses = themeService.getThemes();
        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @ModelAttribute @Valid PopularThemeRequest popularThemeRequest
    ) {
        List<ThemeResponse> themeResponses = themeService.getPopularThemes(popularThemeRequest);
        return ResponseEntity.ok(themeResponses);
    }
}
