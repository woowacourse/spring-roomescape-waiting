package roomescape.theme.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/themes")
public class ThemeRestController {

    private final ThemeService themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            @RequestBody final ThemeRequest themeRequest
    ) {
        final Long id = themeService.save(
                themeRequest.name(),
                themeRequest.description(),
                themeRequest.thumbnail()
        );
        final Theme found = themeService.getById(id);

        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(found));
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Void> deleteTheme(
            @PathVariable final Long id
    ) {
        themeService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        final List<Theme> themes = themeService.findAll();
        final List<ThemeResponse> themeResponses = themes.stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular-list")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        final List<Theme> popularThemes = themeService.findPopularThemes();
        final List<ThemeResponse> themeResponses = popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok(themeResponses);
    }
}
