package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.ThemeCreationRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public List<ThemeResponse> findAllTheme() {
        return themeService.findAllThemes();
    }

    @GetMapping("/ranking")
    public List<ThemeResponse> findTopTheme(@RequestParam("size") int size) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(7);
        return themeService.findTopThemes(from, to, size);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(
            @RequestBody ThemeCreationRequest request
    ) {
        ThemeResponse resDto = themeService.addTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + resDto.id())).body(resDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteThemeById(
            @PathVariable("id") Long id
    ) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }
}
