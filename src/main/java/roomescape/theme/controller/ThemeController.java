package roomescape.theme.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RequireRole;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.MemberRole;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeService.getThemes());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        return ResponseEntity.ok(themeService.getPopularThemes());
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            @RequestBody ThemeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeService.create(request));
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable("id") Long id
    ) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
