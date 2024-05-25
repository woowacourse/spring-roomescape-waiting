package roomescape.web.controller.api;

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
import roomescape.service.ThemeService;
import roomescape.service.request.ThemeSaveDto;
import roomescape.service.response.ThemeDto;
import roomescape.web.controller.request.ThemeRequest;
import roomescape.web.controller.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@Valid @RequestBody ThemeRequest request) {
        ThemeDto appResponse = themeService.save(
                new ThemeSaveDto(request.name(), request.description(), request.thumbnail()));

        Long id = appResponse.id();
        ThemeResponse webResponse = ThemeResponse.from(appResponse);

        return ResponseEntity.created(URI.create("/themes/" + id)).body(webResponse);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll() {
        List<ThemeResponse> response = themeService.findAll()
                .stream()
                .map(ThemeResponse::from).toList();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> findPopular() {
        List<ThemeResponse> response = themeService.findPopular()
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
