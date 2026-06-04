package roomescape.theme.presentation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.application.ThemeService;
import roomescape.theme.presentation.dto.ThemeResponse;

@RestController
@Validated
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService service;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "페이지 번호는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10")
            @Positive(message = "조회 개수는 양수여야 합니다.") int size
    ) {
        List<ThemeResponse> responses = service.getThemes(page, size)
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/weeks/top")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "10")
            @Positive(message = "조회 개수는 양수여야 합니다.") int size
    ) {
        List<ThemeResponse> responses = service.getWeeksTopThemes(startDate, endDate, size)
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
