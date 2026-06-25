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
import roomescape.service.ThemeService;
import roomescape.service.dto.command.ThemeCreateCommand;
import roomescape.service.dto.result.ThemeResult;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResult>> getThemes() {
        final List<ThemeResult> results = themeService.getThemes();
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResult>> getPopularThemes() {
        final List<ThemeResult> results = themeService.getPopularThemes();
        return ResponseEntity.ok().body(results);
    }

    @PostMapping
    public ResponseEntity<ThemeResult> create(
            @Valid @RequestBody final ThemeCreateCommand request
    ) {
        final ThemeResult result = themeService.create(request);
        return ResponseEntity.created(URI.create("/themes/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/{theme-id}")
    public ResponseEntity<Void> delete(
            @PathVariable("theme-id") final Long themeId
    ) {
        themeService.delete(themeId);
        return ResponseEntity.noContent().build();
    }
}
