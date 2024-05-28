package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.domain.dto.ThemeResponse;
import roomescape.service.ThemeService;

import java.time.LocalDate;

@RestController
@RequestMapping("/themes")
public class ClientThemeController {
    private final ThemeService themeService;

    public ClientThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ResponsesWrapper<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeService.findAll());
    }

    @GetMapping("/popular")
    public ResponseEntity<ResponsesWrapper<ThemeResponse>> getPopularTheme(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam Long size) {
        return ResponseEntity.ok(themeService.findPopularTheme(startDate, endDate, size));
    }
}
