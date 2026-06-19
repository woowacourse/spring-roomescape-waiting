package roomescape.theme.web;


import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.Theme;
import roomescape.theme.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
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

    @GetMapping("/{themeId}/times")
    public ResponseEntity<List<AvailableTimeResponseDto>> findAvailableTimesById(@PathVariable Long themeId,
                                                                                 @RequestParam LocalDate localDate) {
        return ResponseEntity.ok(themeService.findAvailableTimesById(themeId, localDate));
    }

    @GetMapping("/populars")
    public ResponseEntity<List<ThemeResponseDto>> findPopulars(
            @Valid @ModelAttribute PopularThemeRequestDto popularThemeRequestDto) {
        List<ThemeResponseDto> responses = themeService.findPopulars(popularThemeRequestDto).stream()
                .map(ThemeResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
