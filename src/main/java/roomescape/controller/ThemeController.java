package roomescape.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.dto.ThemeResponse;
import roomescape.service.ThemeService;

@RequestMapping("/themes")
@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findThemes() {
        List<ThemeResponse> responses = themeService.findThemes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/rankings")
    public ResponseEntity<List<ThemeResponse>> findMostReservedThemes() {
        List<ThemeResponse> responses = themeService.findMostReservedThemes();
        return ResponseEntity.ok().body(responses);
    }
}
