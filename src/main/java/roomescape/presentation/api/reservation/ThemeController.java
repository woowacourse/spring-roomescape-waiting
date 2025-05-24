package roomescape.presentation.api.reservation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.query.ThemeQueryService;
import roomescape.application.reservation.query.dto.ThemeResult;
import roomescape.presentation.api.reservation.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeQueryService themeQueryService;

    public ThemeController(ThemeQueryService themeQueryService) {
        this.themeQueryService = themeQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResult> themeResults = themeQueryService.findAll();
        List<ThemeResponse> themeResponses = themeResults.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ThemeResponse>> findRankBetweenDate() {
        List<ThemeResult> rank = themeQueryService.findWeeklyPopularThemes();
        List<ThemeResponse> themeResponses = rank.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }
}
