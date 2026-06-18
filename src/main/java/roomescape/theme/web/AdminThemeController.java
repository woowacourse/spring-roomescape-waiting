package roomescape.theme.web;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.theme.Theme;
import roomescape.theme.ThemeService;
import roomescape.theme.web.dto.ThemeRequestDto;
import roomescape.theme.web.dto.ThemeResponseDto;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {
    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponseDto> create(@Valid @RequestBody ThemeRequestDto themeRequest) {
        Theme theme = themeService.create(themeRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .build(theme.getId());
        return ResponseEntity.created(uri)
                .body(ThemeResponseDto.from(theme));
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponseDto>> findAll() {
        List<Theme> themes = themeService.findAll();
        List<ThemeResponseDto> responses = themes.stream()
                .map(ThemeResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponseDto> findById(@PathVariable Long id) {
        Theme themeById = themeService.findById(id);
        return ResponseEntity.ok(ThemeResponseDto.from(themeById));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
