package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeCreateRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/api/v1/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            @Valid @RequestBody ThemeCreateRequest themeCreateRequest,
            @LoginMember(role = Role.MANAGER) Member manager) {
        Theme theme = themeService.createTheme(
                themeCreateRequest.name(),
                themeCreateRequest.description(),
                themeCreateRequest.imgUrl()
        );
        ThemeResponse themeResponse = ThemeResponse.from(theme);
        return ResponseEntity.created(URI.create("/api/v1/admin/themes/" + themeResponse.id())).body(themeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable Long id,
            @LoginMember(role = Role.MANAGER) Member manager) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
