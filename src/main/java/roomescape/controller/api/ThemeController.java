package roomescape.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RequestMapping("/themes")
@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> responses = themeService.getAllThemes();

        return ResponseEntity.ok()
                .body(responses);
    }

    @GetMapping("/rankings")
    public ResponseEntity<List<ThemeResponse>> getMostReservedThemes() {
        List<ThemeResponse> responses = themeService.getMostReservedThemes();

        return ResponseEntity.ok()
                .body(responses);
    }
}
