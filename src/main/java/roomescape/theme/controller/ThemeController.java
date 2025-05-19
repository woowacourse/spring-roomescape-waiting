package roomescape.theme.controller;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.utils.UriFactory;
import roomescape.member.auth.PermitAll;
import roomescape.member.auth.RoleRequired;
import roomescape.member.domain.Role;
import roomescape.theme.controller.dto.CreateThemeWebRequest;
import roomescape.theme.controller.dto.ThemeWebResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping(ThemeController.BASE_PATH)
@RequiredArgsConstructor
public class ThemeController {

    public static final String BASE_PATH = "/themes";

    private final ThemeService themeService;

    @GetMapping
    public List<ThemeWebResponse> getAll() {
        return themeService.getAll();
    }

    @PermitAll
    @GetMapping("/ranking")
    public ResponseEntity<List<ThemeWebResponse>> getRanking() {
        return ResponseEntity.ok(themeService.getRanking());
    }

    @RoleRequired(value = Role.ADMIN)
    @PostMapping
    public ResponseEntity<ThemeWebResponse> create(@RequestBody final CreateThemeWebRequest createThemeWebRequest) {
        final ThemeWebResponse themeWebResponse = themeService.create(createThemeWebRequest);
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(themeWebResponse.id()));
        return ResponseEntity.created(location)
                .body(themeWebResponse);
    }

    @RoleRequired(value = Role.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
