package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ApiResponses;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    public static final int POPULAR_THEME_END_DATE_OFFSET = 1;
    private static final String POPULAR_THEME_LIMIT = "10";
    private static final int POPULAR_THEME_MIN_LIMIT = 1;
    private static final int POPULAR_THEME_DAYS_AGO = 6;
    private final ThemeService themeService;
    private final Clock clock;

    public ThemeController(ThemeService themeService, Clock clock) {
        this.themeService = themeService;
        this.clock = clock;
    }

    @GetMapping
    public ApiResponses<ThemeResponse> getAllThemes() {
        List<ThemeResponse> themeResponses = themeService.getAllThemes();
        return new ApiResponses<>(themeResponses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody @Valid ThemeRequest themeRequest) {
        ThemeResponse themeResponse = themeService.addTheme(themeRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id()))
                .body(themeResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteThemeById(@PathVariable Long id) {
        themeService.deleteThemeById(id);
    }

    @GetMapping("/popular")
    public ApiResponses<ThemeResponse> getPopularThemes(@RequestParam(required = false) LocalDate startDate,
                                                        @RequestParam(required = false) LocalDate endDate,
                                                        @RequestParam(defaultValue = POPULAR_THEME_LIMIT) int limit) {
        if (endDate == null) {
            endDate = LocalDate.now(clock).minusDays(POPULAR_THEME_END_DATE_OFFSET);
        }

        if (startDate == null) {
            startDate = endDate.minusDays(POPULAR_THEME_DAYS_AGO);
        }

        if (limit < POPULAR_THEME_MIN_LIMIT) {
            throw new IllegalArgumentException(String.format("limit은 %d 이상이어야 합니다.", POPULAR_THEME_MIN_LIMIT));
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        List<ThemeResponse> themeResponses = themeService.getPopularThemes(startDate, endDate, limit);
        return new ApiResponses<>(themeResponses);
    }
}
