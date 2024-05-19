package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid ThemeRequest themeRequest) {
        ThemeResponse themeResponse = themeService.create(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id())).body(themeResponse);
    }

    @GetMapping
    public List<ThemeResponse> findAllThemes() {
        return themeService.findAll();
    }

    @GetMapping("/popular")
    public List<ThemeResponse> findPopularThemes() {
        return themeService.findPopularThemes();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
