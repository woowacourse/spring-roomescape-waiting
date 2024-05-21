package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.PopularThemeRequest;
import roomescape.service.dto.response.ThemeResponses;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> getThemes() {
        ThemeResponses themeResponses = themeService.getThemes();
        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular")
    public ResponseEntity<ThemeResponses> getPopularThemes(
            @ModelAttribute @Valid PopularThemeRequest popularThemeRequest
    ) {
        ThemeResponses popularThemes = themeService.getPopularThemes(popularThemeRequest);
        return ResponseEntity.ok(popularThemes);
    }
}
