package roomescape.theme.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.response.ThemeReadResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeReadResponse>> getAllThemes() {
        List<ThemeReadResponse> responses = themeService.getAllThemes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeReadResponse>> getPopularThemes(
            @RequestParam("limit") int limit
    ) {
        List<ThemeReadResponse> response = themeService.getPopularThemes(limit);
        return ResponseEntity.ok().body(response);
    }
}
