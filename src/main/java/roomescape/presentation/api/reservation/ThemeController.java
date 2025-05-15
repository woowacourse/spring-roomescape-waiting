package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ThemeService;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.presentation.api.reservation.request.CreateThemeRequest;
import roomescape.presentation.api.reservation.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
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
    public ResponseEntity<ThemeResponse> create(@Valid @RequestBody CreateThemeRequest createThemeRequest) {
        Long id = themeService.create(createThemeRequest.toServiceParam());
        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(themeService.findById(id)));
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> delete(@PathVariable Long themeId) {
        themeService.deleteById(themeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ThemeResponse>> findRankBetweenDate() {
        List<ThemeResult> rank = themeService.findRankBetweenDate();
        List<ThemeResponse> themeResponses = rank.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }
}
