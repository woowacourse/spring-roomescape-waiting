package roomescape.reservation.controller;

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
import roomescape.reservation.controller.dto.ThemeCreateRequest;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.service.dto.ThemeCreateCommand;
import roomescape.reservation.service.dto.ThemeInfo;
import roomescape.reservation.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody @Valid final ThemeCreateRequest request) {
        ThemeCreateCommand command = request.toCommand();
        final ThemeInfo themeInfo = themeService.createTheme(command);
        URI uri = URI.create("/themes/" + themeInfo.id());
        return ResponseEntity.created(uri).body(new ThemeResponse(themeInfo));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        final List<ThemeInfo> themeInfos = themeService.findAll();
        List<ThemeResponse> responses = mapThemeInfoToThemeResponse(themeInfos);
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular-themes")
    public ResponseEntity<List<ThemeResponse>> findPopularThemes() {
        List<ThemeInfo> themeInfos = themeService.findPopularThemes();
        List<ThemeResponse> responses = mapThemeInfoToThemeResponse(themeInfos);
        return ResponseEntity.ok().body(responses);
    }

    private List<ThemeResponse> mapThemeInfoToThemeResponse(final List<ThemeInfo> themeInfos) {
        return themeInfos.stream()
                .map(ThemeResponse::new)
                .toList();
    }
}
