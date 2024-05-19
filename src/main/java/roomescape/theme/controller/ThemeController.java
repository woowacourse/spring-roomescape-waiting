package roomescape.theme.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public List<ThemeResponse> findThemes() {
        return themeService.findThemes();
    }

    @GetMapping("/popular")
    public List<ThemeResponse> findPopularThemes() {
        return themeService.findPopularThemes();
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody ThemeCreateRequest request) {
        ThemeResponse response = themeService.createTheme(request);

        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
    }
}

