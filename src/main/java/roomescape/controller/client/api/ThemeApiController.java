package roomescape.controller.client.api;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.client.api.dto.ThemeResponse;
import roomescape.controller.client.api.dto.ThemeTimesResponse;
import roomescape.query.ThemeQuery;
import roomescape.service.ThemeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/themes")
@Validated
public class ThemeApiController {

    private final ThemeService themeService;
    private final ThemeQuery themeQuery;

    @GetMapping("/{id}/times")
    public ResponseEntity<List<ThemeTimesResponse>> getThemeReservationStatus(
            @PathVariable
            @Positive(message = "테마 조회 식별자는 양수여야 합니다.") Long id,
            @RequestParam LocalDate date
    ) {
        List<ThemeTimesResponse> response = themeService.getThemeReservationStatus(id, date)
                .stream()
                .map(ThemeTimesResponse::from)
                .toList();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        return ResponseEntity.ok().body(themeQuery.getAllActiveThemes());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(@RequestParam LocalDate startDate) {
        return ResponseEntity.ok().body(themeQuery.getPopularThemes(startDate, LocalDate.now()));
    }
}
