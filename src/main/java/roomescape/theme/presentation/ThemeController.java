package roomescape.theme.presentation;

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
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.theme.presentation.dto.request.ThemeCreateRequest;
import roomescape.theme.presentation.dto.response.ThemeResponse;
import roomescape.theme.application.ThemeApplicationService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private static final int POPULAR_THEMES_DAYS = 7;
    private static final int POPULAR_THEMES_LIMIT = 10;

    private final ThemeApplicationService themeApplicationService;

    public ThemeController(final ThemeApplicationService themeApplicationService) {
        this.themeApplicationService = themeApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    public ResponseEntity<ThemeResponse> create(
            @RequestBody ThemeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeApplicationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        return ResponseEntity.ok(themeApplicationService.findAll());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> findPopular() {
        return ResponseEntity.ok(themeApplicationService.findPopular(POPULAR_THEMES_DAYS, POPULAR_THEMES_LIMIT));
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        themeApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
