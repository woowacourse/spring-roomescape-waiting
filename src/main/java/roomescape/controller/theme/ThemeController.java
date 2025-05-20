package roomescape.controller.theme;

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
import roomescape.domain.theme.Theme;
import roomescape.dto.theme.AddThemeDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.service.theme.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponseDto> addTheme(@Valid @RequestBody AddThemeDto addThemeDto) {
        long id = themeService.addTheme(addThemeDto);
        Theme theme = themeService.getThemeById(id);
        ThemeResponseDto themeResponseDto = new ThemeResponseDto(theme.getId(), theme.getDescription(), theme.getName(),
                theme.getThumbnail());
        return ResponseEntity.created(URI.create("/themes/" + id)).body(themeResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponseDto> getTheme(@PathVariable Long id) {
        Theme theme = themeService.getThemeById(id);
        ThemeResponseDto themeResponseDto = new ThemeResponseDto(theme.getId(), theme.getDescription(), theme.getName(),
                theme.getThumbnail());
        return ResponseEntity.ok(themeResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponseDto>> getThemes() {
        List<Theme> themes = themeService.findAll();
        List<ThemeResponseDto> themeResponseDtos = themes.stream()
                .map((theme) -> new ThemeResponseDto(theme.getId(), theme.getDescription(),
                        theme.getName(), theme.getThumbnail()))
                .toList();
        return ResponseEntity.ok(themeResponseDtos);
    }
}
