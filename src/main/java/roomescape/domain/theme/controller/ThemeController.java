package roomescape.domain.theme.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddCommand;
import roomescape.domain.theme.dto.ThemeAddRequest;
import roomescape.domain.theme.dto.ThemeRankingResponse;
import roomescape.domain.theme.service.ThemeService;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<Theme>> getThemeList() {
        return ResponseEntity.ok(themeService.findAllTheme());
    }

    @PostMapping("/themes")
    public ResponseEntity<Theme> addTheme(@RequestBody ThemeAddRequest themeAddRequest) {
        ThemeAddCommand themeAddCommand = ThemeAddCommand.from(themeAddRequest);

        Theme theme = themeService.addTheme(themeAddCommand);
        return ResponseEntity.created(URI.create("/themes" + theme.getId())).body(theme);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") Long id) {
        themeService.removeTheme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/themes/ranking")
    public ResponseEntity<List<ThemeRankingResponse>> getThemeRank() {
        List<ThemeRankingResponse> themeRankingResponses = themeService.getThemeRanking()
                .stream()
                .map(ThemeRankingResponse::from)
                .toList();
        return ResponseEntity.ok(themeRankingResponses);
    }
}
