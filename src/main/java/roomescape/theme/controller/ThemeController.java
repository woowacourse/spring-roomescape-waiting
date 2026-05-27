package roomescape.theme.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.controller.dto.ThemesResponse;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemesResponse> list() {
        return ResponseEntity.ok(
                ThemesResponse.from(themeService.list()
                        .stream()
                        .map(ThemeResponse::from)
                        .toList())
        );
    }

    @GetMapping("/popularity")
    public ResponseEntity<ThemesResponse> popularThemes(
            @Positive(message = "조회 기간은 양수여야 합니다.") @RequestParam("days") int days,
            @Positive(message = "조회 개수는 양수여야 합니다.") @RequestParam("size") int size) {
        return ResponseEntity.ok(
                ThemesResponse.from(themeService.findPopularThemes(days, LocalDate.now(), size)
                        .stream()
                        .map(ThemeResponse::from)
                        .toList())
        );
    }
}
