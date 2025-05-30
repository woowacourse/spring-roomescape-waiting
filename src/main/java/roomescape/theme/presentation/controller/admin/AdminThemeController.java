package roomescape.theme.presentation.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.theme.application.ThemeApplicationService;
import roomescape.theme.presentation.dto.request.ThemeCreateWebRequest;
import roomescape.theme.presentation.dto.response.ThemeWebResponse;

@RestController
public class AdminThemeController {

    private final ThemeApplicationService themeApplicationService;

    public AdminThemeController(final ThemeApplicationService themeApplicationService) {
        this.themeApplicationService = themeApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeWebResponse> create(
            @RequestBody ThemeCreateWebRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeApplicationService.create(request));
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/admin/themes/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        themeApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
