package roomescape.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.dto.ThemeRequest;
import roomescape.domain.dto.ThemeResponse;
import roomescape.service.ThemeService;

import java.net.URI;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {
    private final ThemeService themeService;

    public AdminThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody final ThemeRequest themeRequest) {
        ThemeResponse themeResponse = themeService.create(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id())).body(themeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
