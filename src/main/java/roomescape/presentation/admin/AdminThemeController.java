package roomescape.presentation.admin;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ThemeService;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;

@RestController
@RequestMapping("/admin")
public class AdminThemeController {
    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> create(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse response = themeService.save(themeRequest);
        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
