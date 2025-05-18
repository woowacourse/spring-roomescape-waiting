package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ThemeService;
import roomescape.application.dto.ThemeCreateServiceRequest;
import roomescape.application.dto.ThemeServiceResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public List<ThemeServiceResponse> getAllThemes() {
        return themeService.getAllThemes();
    }

    @PostMapping
    public ResponseEntity<ThemeServiceResponse> createTheme(
            @Valid @RequestBody ThemeCreateServiceRequest themeRequest
    ) {
        ThemeServiceResponse themeServiceResponse = themeService.registerTheme(themeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(themeServiceResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ranking")
    public List<ThemeServiceResponse> getThemeRanking() {
        return themeService.getThemeRanking();
    }
}
