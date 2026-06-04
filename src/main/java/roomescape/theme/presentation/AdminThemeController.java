package roomescape.theme.presentation;

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
import roomescape.theme.application.ThemeService;
import roomescape.theme.presentation.response.AdminThemeResponse;
import roomescape.theme.presentation.request.CreateThemeRequest;
import roomescape.theme.presentation.response.CreateThemeResponse;
import roomescape.common.auth.AdminRequestValidator;

@RestController
@RequiredArgsConstructor
public class AdminThemeController {

    private final ThemeService themeService;
    private final AdminRequestValidator validator;

    @GetMapping("/admin/themes")
    public ResponseEntity<List<AdminThemeResponse>> getAllThemeForAdmin(HttpServletRequest request) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AdminThemeResponse> response = themeService.getAllThemeForAdmin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<CreateThemeResponse> createTheme(
        @Valid @RequestBody CreateThemeRequest createThemeRequest,
        HttpServletRequest httpServletRequest
    ) {
        if (validator.isUnauthorized(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CreateThemeResponse response = themeService.createTheme(createThemeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id, HttpServletRequest request) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        themeService.deleteTheme(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
