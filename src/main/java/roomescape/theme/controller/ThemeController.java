package roomescape.theme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.controller.dto.PopularityRequest;
import roomescape.theme.controller.dto.ThemeListResponse;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<ThemeListResponse> getAllThemes() {
        return ResponseEntity.ok(
                ThemeListResponse.from(themeService.findAllThemes()
                        .stream()
                        .map(ThemeResponse::from)
                        .toList())
        );
    }

    @GetMapping("/popularity")
    public ResponseEntity<ThemeListResponse> popularThemes(
            @ModelAttribute @Valid PopularityRequest request
    ) {
        return ResponseEntity.ok(
                ThemeListResponse.from(themeService.findPopularThemes(request.days(), request.size())
                        .stream()
                        .map(ThemeResponse::from)
                        .toList())
        );
    }
}
