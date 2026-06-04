package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeCommandService;
import roomescape.service.ThemeQueryService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/themes")
@RequiredArgsConstructor
public class AdminThemeController {

    private final ThemeCommandService themeCommandService;
    private final ThemeQueryService themeQueryService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<Theme> allThemes = themeQueryService.findAllThemes();
        List<ThemeResponse> themeResponses = allThemes.stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(themeResponses);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest request) {
        Theme theme = themeCommandService.create(request.name(), request.thumbnailUrl(), request.description());
        Long savedId = theme.id();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(ThemeResponse.from(theme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") Long id) {
        themeCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
