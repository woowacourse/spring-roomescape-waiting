package roomescape.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.auth.AuthService;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeResponse;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final AuthService authService;

    public ThemeController(ThemeService themeService, AuthService authService) {
        this.themeService = themeService;
        this.authService = authService;
    }

    @GetMapping
    public List<ThemeResponse> findAllThemes() {
        return themeService.findAll();
    }

    @GetMapping("/popular")
    public List<ThemeResponse> findPopularThemes() {
        return themeService.findPopularThemes();
    }
}
