package roomescape.theme.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.ThemeRankResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse themeCreateRequestResult = themeService.addTheme(themeRequest);
        URI uri = URI.create("/themes/" + themeCreateRequestResult.id());
        return ResponseEntity.created(uri)
                .body(themeCreateRequestResult);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> themeList() {
        List<ThemeResponse> themeListRequestResult = themeService.findThemes();
        return ResponseEntity.ok()
                .body(themeListRequestResult);
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ThemeRankResponse>> themeRankList() {
        List<ThemeRankResponse> rankedThemesListRequestResult = themeService.findRankedThemes();
        return ResponseEntity.ok()
                .body(rankedThemesListRequestResult);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") long themeId) {
        themeService.removeTheme(themeId);
        return ResponseEntity.noContent()
                .build();
    }

}
