package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.auth.AuthService;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

import java.net.URI;
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
