package roomescape.theme.controller;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeServiceFacade;

@RestController
@AllArgsConstructor
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeServiceFacade themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody ThemeCreateRequest themeCreateRequest) {
        ThemeResponse response = themeService.createTheme(themeCreateRequest);
        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResponse> themes = themeService.findAll();
        return ResponseEntity.ok(themes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lists")
    public ResponseEntity<List<ThemeResponse>> findPopular() {
        List<ThemeResponse> listedTheme = themeService.findPopular();
        return ResponseEntity.ok(listedTheme);
    }
}
