package roomescape.feature.theme.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.theme.service.ThemeService;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponseDto>> getThemes() {
        return ResponseEntity.ok(themeService.getThemes());
    }

    @GetMapping("/rankings/last-7-days")
    public ResponseEntity<List<ThemeResponseDto>> getPopularThemes() {
        return ResponseEntity.ok(themeService.getPopularThemes());
    }
}
