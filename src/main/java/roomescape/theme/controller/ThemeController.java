package roomescape.theme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeServiceFacade;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeServiceFacade themeService;

    @Autowired
    public ThemeController(ThemeServiceFacade themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody ThemeCreateRequest themeCreateRequest) {
        ThemeResponse response = themeService.createTheme(themeCreateRequest);
        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themes = themeService.findAll();
        return ResponseEntity.ok(themes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lists")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        List<ThemeResponse> listedTheme = themeService.findLimitedThemesByPopularDesc();
        return ResponseEntity.ok(listedTheme);
    }
}
