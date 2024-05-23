package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<ThemeResponse>> findAllRoomThemes() {
        return ResponseEntity.ok(themeService.findAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<ListResponse<ThemeResponse>> findAllRoomThemesRanking() {
        return ResponseEntity.ok(themeService.findByRanking());
    }
}
