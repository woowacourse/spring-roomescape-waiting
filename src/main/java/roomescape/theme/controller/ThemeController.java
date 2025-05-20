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
import roomescape.auth.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeDomainService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private static final int POPULAR_THEMES_DAYS = 7;
    private static final int POPULAR_THEMES_LIMIT = 10;

    private final ThemeDomainService themeDomainService;

    public ThemeController(final ThemeDomainService themeDomainService) {
        this.themeDomainService = themeDomainService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeDomainService.getThemes());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        return ResponseEntity.ok(themeDomainService.getPopularThemes(POPULAR_THEMES_DAYS, POPULAR_THEMES_LIMIT));
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            @RequestBody ThemeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeDomainService.create(request));
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable("id") Long id
    ) {
        themeDomainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
