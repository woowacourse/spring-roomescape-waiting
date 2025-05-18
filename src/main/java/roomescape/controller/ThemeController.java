package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.CreateThemeRequest;
import roomescape.controller.response.ThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.result.ThemeResult;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResult> themeResults = themeService.findAll();
        List<ThemeResponse> themeResponses = themeResults.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody CreateThemeRequest createThemeRequest) {
        Long id = themeService.create(createThemeRequest.toServiceParam());
        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(themeService.findById(id)));
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> delete(@PathVariable Long themeId) {
        themeService.deleteById(themeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ThemeResponse>> rank() {
        List<ThemeResult> rankForWeek = themeService.findRankByTheme();

        List<ThemeResponse> themeResponses = rankForWeek.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }
}
