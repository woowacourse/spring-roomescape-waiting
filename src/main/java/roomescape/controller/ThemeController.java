package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.request.CreateThemeRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.dto.result.ThemeResult;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAll() {
        List<ThemeResult> themeResults = themeService.getAll();
        List<ThemeResponse> themeResponses = themeResults.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@Valid @RequestBody CreateThemeRequest createThemeRequest) {
        ThemeResult themeResult = themeService.create(createThemeRequest.toServiceParam());
        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(themeResult));
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> delete(@PathVariable Long themeId) {
        themeService.deleteById(themeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ThemeResponse>> getRankingTheme() {
        List<ThemeResult> rankForWeek = themeService.getRankByTheme();

        List<ThemeResponse> themeResponses = rankForWeek.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }
}
