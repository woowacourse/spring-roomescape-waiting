package roomescape.theme.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;

import jakarta.validation.Valid;
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
import roomescape.auth.domain.RequiresRole;
import roomescape.theme.application.ThemeService;
import roomescape.theme.ui.dto.CreateThemeRequest;
import roomescape.theme.ui.dto.ThemeResponse;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeRestController {

    private final ThemeService themeService;

    @PostMapping
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<ThemeResponse> create(
            @RequestBody @Valid final CreateThemeRequest request
    ) {
        final ThemeResponse response = themeService.create(request);

        return ResponseEntity.created(URI.create("/themes/" + response.id()))
                .body(response);
    }

    @DeleteMapping({"/{id}"})
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<Void> delete(
            @PathVariable final Long id
    ) {
        themeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        final List<ThemeResponse> themeResponses = themeService.findAll();

        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular-list")
    public ResponseEntity<List<ThemeResponse>> findPopularThemes() {
        final List<ThemeResponse> popularThemes = themeService.findPopularThemes();

        return ResponseEntity.ok(popularThemes);
    }
}
