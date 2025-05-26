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
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.facade.ThemeServiceFacade;

@RestController
@RequiredArgsConstructor
@RequestMapping("/themes")
public class ThemeRestController {

    private final ThemeServiceFacade themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(
            @RequestBody final ThemeRequest themeRequest
    ) {
        final ThemeResponse response = themeService.saveTheme(themeRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Void> deleteTheme(
            @PathVariable final Long id
    ) {
        themeService.deleteTheme(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        final List<ThemeResponse> themeResponses = themeService.getThemes();

        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/popular-list")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        final List<ThemeResponse> popularThemes = themeService.getPopularThemes();

        return ResponseEntity.ok(popularThemes);
    }
}
