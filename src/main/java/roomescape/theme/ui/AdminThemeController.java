package roomescape.theme.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.auth.aop.RequiredRoles;
import roomescape.theme.application.ThemeFacade;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.theme.ui.dto.CreateThemeWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;

@RestController
@RequestMapping("/themes")
@RequiredRoles(UserRole.ADMIN)
@RequiredArgsConstructor
public class AdminThemeController {

    private final ThemeFacade themeFacade;

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody final CreateThemeWebRequest createThemeWebRequest) {
        final ThemeResponse response = themeFacade.create(createThemeWebRequest.toServiceRequest());

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        themeFacade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
