package roomescape.theme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.request.ThemeCreateRequest;
import roomescape.theme.service.dto.response.ThemeResponse;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        final List<ThemeResponse> results = themeService.getPopularThemes();
        return ResponseEntity.ok().body(results);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(
            @Valid @RequestBody ThemeCreateRequest request
    ) {
        final ThemeResponse result = themeService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @DeleteMapping("/{theme-id}")
    public ResponseEntity<Void> delete(
            @PathVariable("theme-id") Long themeId
    ) {
        themeService.delete(themeId);
        return ResponseEntity.noContent().build();
    }
}
