package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

import java.net.URI;

@RequestMapping("/admin/themes")
@RestController
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.addTheme(request);
        URI location = URI.create("/themes/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteThemeById(id);

        return ResponseEntity.noContent()
                .build();
    }
}
