package roomescape.web.api;

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
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.policy.WeeklyRankingPolicy;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.theme.ThemeRequest;
import roomescape.service.dto.response.theme.ThemeResponse;

@RestController
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> findAllTheme() {
        List<ThemeResponse> response = themeService.findAllTheme();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/themes/ranking")
    public ResponseEntity<List<ThemeResponse>> findAllPopularTheme() {
        List<ThemeResponse> response = themeService.findAllPopularThemes(new WeeklyRankingPolicy());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> saveTheme(@Valid @RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.saveTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + response.id())).body(response);
    }

    @DeleteMapping("/themes/{theme_id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable(value = "theme_id") Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
