package roomescape.theme;

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
import roomescape.member.Member;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;

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
