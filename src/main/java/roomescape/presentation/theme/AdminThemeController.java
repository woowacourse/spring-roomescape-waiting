package roomescape.presentation.theme;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.theme.ThemeService;
import roomescape.application.theme.request.CreateThemeRequest;
import roomescape.application.theme.response.AdminThemesResponse;
import roomescape.application.theme.response.CreateThemeResponse;

@RestController
@RequiredArgsConstructor
public class AdminThemeController {

    private final ThemeService themeService;

    @GetMapping("/admin/themes")
    public ResponseEntity<AdminThemesResponse> getAllThemeForAdmin() {
        return ResponseEntity.ok(themeService.getAllThemeForAdmin());
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<CreateThemeResponse> createTheme(
            @Valid @RequestBody CreateThemeRequest createThemeRequest
    ) {
        CreateThemeResponse response = themeService.createTheme(createThemeRequest);
        return ResponseEntity.created(URI.create("/themes/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
