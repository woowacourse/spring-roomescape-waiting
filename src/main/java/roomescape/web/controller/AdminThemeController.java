package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.theme.Theme;
import roomescape.service.ThemeCommandService;
import roomescape.service.ThemeQueryService;
import roomescape.web.dto.request.ThemeRequest;
import roomescape.web.dto.response.ThemeResponse;

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
    public ResponseEntity<ThemeResponse> createTheme(
            @Valid @RequestBody ThemeRequest request
    ) {
        Theme theme = themeCommandService.create(ThemeRequest.toCommand(request));
        Long savedId = theme.getId();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(ThemeResponse.from(theme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable Long id
    ) {
        themeCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
