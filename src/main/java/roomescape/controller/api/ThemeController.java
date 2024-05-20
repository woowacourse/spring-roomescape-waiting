package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

import java.util.List;

@RequestMapping("/themes")
@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllThemes();
        MultipleResponse<ThemeResponse> response = new MultipleResponse<>(themes);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/rankings")
    public ResponseEntity<MultipleResponse<ThemeResponse>> getMostReservedThemes() {
        List<ThemeResponse> themes = themeService.getMostReservedThemes();
        MultipleResponse<ThemeResponse> response = new MultipleResponse<>(themes);

        return ResponseEntity.ok()
                .body(response);
    }
}
