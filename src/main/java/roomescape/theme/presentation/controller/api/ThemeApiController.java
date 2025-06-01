package roomescape.theme.presentation.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.presentation.dto.ThemeCreateRequest;
import roomescape.theme.presentation.dto.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeApiController {

    private final ThemeService themeService;

    public ThemeApiController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody @Valid final ThemeCreateRequest request) {
        final ThemeCreateCommand command = request.convertToCreateCommand();
        final ThemeInfo themeInfo = themeService.createTheme(command);
        final URI uri = URI.create("/themes/" + themeInfo.id());
        return ResponseEntity.created(uri).body(new ThemeResponse(themeInfo));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        final List<ThemeInfo> themeInfos = themeService.findThemes();
        final List<ThemeResponse> responses = mapThemeInfoToThemeResponse(themeInfos);
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular-themes")
    public ResponseEntity<List<ThemeResponse>> findPopularThemes() {
        final List<ThemeInfo> themeInfos = themeService.findPopularThemes();
        final List<ThemeResponse> responses = mapThemeInfoToThemeResponse(themeInfos);
        return ResponseEntity.ok().body(responses);
    }

    private List<ThemeResponse> mapThemeInfoToThemeResponse(final List<ThemeInfo> themeInfos) {
        return themeInfos.stream()
                .map(ThemeResponse::new)
                .toList();
    }
}
