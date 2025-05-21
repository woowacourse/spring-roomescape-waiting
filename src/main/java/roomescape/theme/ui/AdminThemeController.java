package roomescape.theme.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.aop.RequiredRoles;
import roomescape.common.uri.UriFactory;
import roomescape.theme.application.ThemeFacade;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.theme.ui.dto.CreateThemeWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;

@RestController
@RequestMapping(AdminThemeController.BASE_PATH)
@RequiredRoles(UserRole.ADMIN)
@RequiredArgsConstructor
public class AdminThemeController {

    public static final String BASE_PATH = "/themes";

    private final ThemeFacade themeFacade;

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody final CreateThemeWebRequest createThemeWebRequest) {
        final ThemeResponse themeResponse = themeFacade.create(createThemeWebRequest.toServiceRequest());
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(themeResponse.id()));
        return ResponseEntity.created(location)
                .body(themeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        themeFacade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
