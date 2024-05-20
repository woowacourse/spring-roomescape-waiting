package roomescape.presentation.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ThemeService;
import roomescape.application.dto.request.ThemeRequest;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.exception.BadRequestException;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private static final String POPULAR_THEME_LIMIT = "10";
    private static final int POPULAR_THEME_MIN_LIMIT = 1;
    private static final int POPULAR_THEME_DAYS_AGO = 6;
    public static final int POPULAR_THEME_END_DATE_OFFSET = 1;

    private final ThemeService themeService;
    private final Clock clock;

    public ThemeController(ThemeService themeService, Clock clock) {
        this.themeService = themeService;
        this.clock = clock;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themeResponses = themeService.getAllThemes();

        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = POPULAR_THEME_LIMIT) int limit
    ) {
        if (endDate == null) {
            endDate = LocalDate.now(clock).minusDays(POPULAR_THEME_END_DATE_OFFSET);
        }

        if (startDate == null) {
            startDate = endDate.minusDays(POPULAR_THEME_DAYS_AGO);
        }

        if (limit < POPULAR_THEME_MIN_LIMIT) {
            throw new BadRequestException(String.format("limit은 %d 이상이어야 합니다.", POPULAR_THEME_MIN_LIMIT));
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        List<ThemeResponse> themeResponses = themeService.getPopularThemes(startDate, endDate, limit);

        return ResponseEntity.ok(themeResponses);
    }
}
