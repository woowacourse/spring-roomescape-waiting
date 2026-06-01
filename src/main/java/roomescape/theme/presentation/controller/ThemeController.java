package roomescape.theme.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.presentation.dto.ThemeResponse;

@RequiredArgsConstructor
@RequestMapping("/themes")
@RestController
public class ThemeController {

    private final ThemeQueryService themeQueryService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        return ResponseEntity.ok(
                themeQueryService.findAll().stream()
                        .map(ThemeResponse::from)
                        .toList()
        );
    }

    @GetMapping("/popular-top-10")
    public ResponseEntity<List<PopularThemeResponse>> findPopularThemes() {
        return ResponseEntity.ok(
                themeQueryService.findPopularThemes(LocalDate.now()).stream()
                        .map(PopularThemeResponse::from)
                        .toList()
        );
    }
}
