package roomescape.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.RoleAllowed;
import roomescape.domain.MemberRole;
import roomescape.service.ThemeService;
import roomescape.service.dto.ThemeListResponse;
import roomescape.service.dto.ThemeRequest;
import roomescape.service.dto.ThemeResponse;

@RestController
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public ResponseEntity<ThemeListResponse> findAllTheme() {
        ThemeListResponse response = themeService.findAllTheme();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/themes/popular")
    public ResponseEntity<ThemeListResponse> findAllPopularTheme() {
        ThemeListResponse response = themeService.findAllPopularTheme();
        return ResponseEntity.ok().body(response);
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> saveTheme(@RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.saveTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + response.getId())).body(response);
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @DeleteMapping("/themes/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }
}
