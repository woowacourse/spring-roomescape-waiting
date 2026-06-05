package roomescape.controller;

import jakarta.validation.Valid;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.controller.dto.response.ThemeResponses;
import roomescape.domain.theme.Theme;
import roomescape.service.ThemeService;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/admin/themes")
    public ResponseEntity<ThemeResponse> create(@Valid @RequestBody ThemeCreateRequest request) {
        Theme theme = themeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(theme.getId())
                .toUri();

        return ResponseEntity.created(location).body(ThemeResponse.toDto(theme));
    }

    @GetMapping("/themes/{id}")
    public ResponseEntity<ThemeResponse> find(@PathVariable long id) {
        Theme theme = themeService.find(id);
        return ResponseEntity.ok(ThemeResponse.toDto(theme));
    }

    @GetMapping("/themes/famous")
    public ResponseEntity<ThemeResponses> findFamous(@Valid @ModelAttribute ThemeFamousFindRequest request) {
        List<Theme> themes = themeService.findFamous(request, LocalDate.now());
        return ResponseEntity.ok(ThemeResponses.toDto(themes));
    }

    @GetMapping("/themes")
    public ResponseEntity<ThemeResponses> findAll() {
        List<Theme> themes = themeService.findAll();
        return ResponseEntity.ok(ThemeResponses.toDto(themes));
    }

    @DeleteMapping({"/admin/themes/{id}"})
    public ResponseEntity<Void> delete(@PathVariable long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
