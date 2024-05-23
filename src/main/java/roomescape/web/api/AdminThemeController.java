package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.theme.ThemeRequest;
import roomescape.service.dto.response.theme.ThemeResponse;

@RestController
@RequiredArgsConstructor
public class AdminThemeController {
    private final ThemeService themeService;

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> saveTheme(@RequestBody @Valid ThemeRequest request) {
        ThemeResponse response = themeService.saveTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + response.id())).body(response);
    }

    @DeleteMapping("/themes/{idTheme}")
    public ResponseEntity<Void> deleteTheme(@PathVariable(value = "idTheme") Long themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }
}
