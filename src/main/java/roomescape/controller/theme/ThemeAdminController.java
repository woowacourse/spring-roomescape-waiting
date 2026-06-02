package roomescape.controller.theme;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.theme.dto.ThemeCreateRequest;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.theme.ThemeService;

@RestController
@RequestMapping("/admin/themes")
public class ThemeAdminController {

    private final ThemeService themeService;

    public ThemeAdminController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themes = themeService.getAll().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok()
                .body(themes);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody final ThemeCreateRequest themeCreateRequest) {
        ThemeResponse theme = ThemeResponse.from(themeService.save(
                themeCreateRequest.name(),
                themeCreateRequest.description(),
                themeCreateRequest.thumbnailUrl()));
        return ResponseEntity.created(URI.create("/admin/themes/" + theme.id()))
                .body(theme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable final Long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
