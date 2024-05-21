package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.ThemeCreateRequest;
import roomescape.controller.api.dto.response.ThemeResponse;
import roomescape.controller.api.dto.response.ThemesResponse;
import roomescape.service.ThemeService;
import roomescape.service.dto.output.ThemeOutput;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeApiController {

    private final ThemeService themeService;

    public ThemeApiController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody final ThemeCreateRequest request) {
        final ThemeOutput output = themeService.createTheme(request.toInput());
        return ResponseEntity.created(URI.create("/times/" + output.id()))
                .body(ThemeResponse.toResponse(output));
    }

    @GetMapping
    public ResponseEntity<ThemesResponse> getAllThemes() {
        final List<ThemeOutput> outputs = themeService.getAllThemes();
        return ResponseEntity.ok()
                .body(ThemesResponse.toResponse(outputs));
    }

    @GetMapping("/popular")
    public ResponseEntity<ThemesResponse> getPopularThemes(@RequestParam final LocalDate date) {
        final List<ThemeOutput> outputs = themeService.getPopularThemes(date);
        return ResponseEntity.ok()
                .body(ThemesResponse.toResponse(outputs));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable final long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent()
                .build();
    }
}
