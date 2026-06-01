package roomescape.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Theme;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeQueryService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
@Validated
public class ThemeController {

    private final ThemeQueryService themeQueryService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<Theme> allThemes = themeQueryService.findAllThemes();
        List<ThemeResponse> themeResponses = allThemes.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getSortedPopularThemesAtPeriod(
            @RequestParam("limit") @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.") int limit) {

        LocalDate today = LocalDate.now(clock);
        LocalDate startAt = today.minusWeeks(1L);
        LocalDate endAt = today.minusDays(1);

        List<Theme> popularThemesBy = themeQueryService.findPopularThemesBy(startAt, endAt, limit);

        List<ThemeResponse> themeResponses = popularThemesBy.stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok(themeResponses);
    }
}
