package roomescape.domain.theme;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.theme.dto.AdminThemeResponse;
import roomescape.domain.theme.dto.ThemeCreationRequest;
import roomescape.domain.theme.dto.ThemeCreationResponse;

@RestController
@RequiredArgsConstructor
public class AdminThemeController {

    private final ThemeService themeService;

    @GetMapping("/admin/themes")
    public ResponseEntity<List<AdminThemeResponse>> getAllThemeForAdmin() {
        List<AdminThemeResponse> response = themeService.getAllThemeForAdmin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeCreationResponse> createTheme(
        @Valid @RequestBody ThemeCreationRequest createThemeRequest) {
        ThemeCreationResponse response = themeService.createTheme(createThemeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
