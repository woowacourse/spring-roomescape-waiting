package roomescape.theme;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@RestController
@RequestMapping("/themes")
@AllArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> create(
            @RequestBody @Valid final ThemeRequest request
    ) {
        final ThemeResponse response = themeService.create(request);
        return ResponseEntity
                .created(URI.create("/themes/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> readAll() {
        return ResponseEntity.ok(themeService.findAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ThemeResponse>> readTopRankThemes(
            @RequestParam(value = "size", defaultValue = "10") final int size
    ) {
        final List<ThemeResponse> topRankThemes = themeService.findTopRank(size);
        return ResponseEntity.ok(topRankThemes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable("id") final Long id
    ) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
