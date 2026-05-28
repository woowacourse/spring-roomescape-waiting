package roomescape.admin.theme;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.theme.dto.AdminThemeRequest;
import roomescape.admin.theme.dto.AdminThemeResponse;
import roomescape.admin.theme.dto.AdminThemesResponse;

@RestController
public class AdminThemeController {

    private final AdminThemeService adminThemeService;

    public AdminThemeController(AdminThemeService adminThemeService) {
        this.adminThemeService = adminThemeService;
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<AdminThemeResponse> createTheme(
        @RequestBody @Valid AdminThemeRequest request
    ) {
        AdminThemeResponse response = adminThemeService.createTheme(request);
        URI location = URI.create("/admin/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/admin/themes")
    public ResponseEntity<AdminThemesResponse> getThemes() {
        AdminThemesResponse responses = adminThemeService.getAllThemes();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> deleteTheme(
        @PathVariable Long id
    ) {
        adminThemeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
