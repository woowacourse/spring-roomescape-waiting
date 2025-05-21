package roomescape.admin.theme;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.theme.application.ThemeService;
import roomescape.theme.presentation.ThemeRequest;
import roomescape.theme.presentation.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @Auth(Role.ADMIN)
    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            final @RequestBody ThemeRequest request
    ) {
        ThemeResponse theme = themeService.createTheme(request);

        return ResponseEntity.created(createUri(theme.getId())).body(theme);
    }

    @Auth(Role.ADMIN)
    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(
            final @PathVariable Long themeId
    ) {
        themeService.deleteTheme(themeId);

        return ResponseEntity.noContent().build();
    }

    private URI createUri(Long themeId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(themeId)
                .toUri();
    }
}
