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
import roomescape.service.request.ThemeAppRequest;
import roomescape.service.response.ThemeAppResponse;
import roomescape.web.controller.request.ThemeWebRequest;
import roomescape.web.controller.response.ThemeWebResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeWebResponse> create(@Valid @RequestBody ThemeWebRequest request) {
        ThemeAppResponse appResponse = themeService.save(
            new ThemeAppRequest(request.name(), request.description(), request.thumbnail()));

        Long id = appResponse.id();
        ThemeWebResponse webResponse = ThemeWebResponse.from(appResponse);

        return ResponseEntity.created(URI.create("/themes/" + id)).body(webResponse);
    }

    @GetMapping
    public ResponseEntity<List<ThemeWebResponse>> findAll() {
        List<ThemeWebResponse> response = themeService.findAll()
            .stream()
            .map(ThemeWebResponse::from).toList();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeWebResponse>> findPopular() {
        List<ThemeWebResponse> response = themeService.findPopular()
            .stream()
            .map(ThemeWebResponse::from)
            .toList();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
