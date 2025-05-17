package roomescape.reservation.presentation.controller;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.service.ThemeService;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
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

    @Auth(Role.USER)
    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes(
    ) {
        return ResponseEntity.ok().body(
                themeService.getThemes()
        );
    }

    @Auth(Role.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            final @PathVariable Long id
    ) {
        themeService.deleteTheme(id);

        return ResponseEntity.noContent().build();
    }

    @Auth(Role.GUEST)
    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
    ) {
        return ResponseEntity.ok().body(
                themeService.getPopularThemes()
        );
    }

    private URI createUri(Long themeId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(themeId)
                .toUri();
    }
}
