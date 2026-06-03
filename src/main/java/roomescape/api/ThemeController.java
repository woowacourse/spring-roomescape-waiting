package roomescape.api;

import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final Clock clock;

    public ThemeController(ThemeService themeService, Clock clock) {
        this.themeService = themeService;
        this.clock = clock;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResponse> responses = themeService.getThemes().stream()
            .map(ThemeResponse::from)
            .toList();

        return ResponseEntity.ok().body(responses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> add(@Valid @RequestBody ThemeRequest request) {
        ThemeResponse response = ThemeResponse.from(themeService.addTheme(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.deleteTheme(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = {"days"})
    public ResponseEntity<List<ThemeResponse>> searchPopularTop10(@RequestParam(defaultValue = "7") Integer days) {
        List<ThemeResponse> responses = themeService.getPopularTop10Themes(LocalDate.now(clock), days).stream()
            .map(ThemeResponse::from)
            .toList();

        return ResponseEntity.ok().body(responses);
    }
}
