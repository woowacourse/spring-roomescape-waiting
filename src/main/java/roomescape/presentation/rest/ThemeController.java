package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ThemeService;
import roomescape.domain.theme.Theme;
import roomescape.presentation.request.CreateThemeRequest;
import roomescape.presentation.response.ThemeResponse;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/admin/themes")
    @ResponseStatus(CREATED)
    public ThemeResponse addTheme(
            @Valid @RequestBody final CreateThemeRequest request
    ) {
        Theme theme = themeService.saveTheme(request.name(), request.description(), request.thumbnail());

        return ThemeResponse.fromTheme(theme);
    }

    @GetMapping("/themes")
    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeService.findAllThemes();

        return ThemeResponse.fromThemes(themes);
    }

    @GetMapping("/themes/popular")
    public List<ThemeResponse> findPopularThemes(
            @RequestParam final LocalDate startDate,
            @RequestParam final LocalDate endDate,
            @RequestParam final int count
    ) {
        List<Theme> popularThemes = themeService.findPopularThemes(startDate, endDate, count);

        return ThemeResponse.fromThemes(popularThemes);
    }

    @DeleteMapping("/admin/themes/{id}")
    @ResponseStatus(NO_CONTENT)
    public void removeTheme(
            @PathVariable final long id
    ) {
        themeService.removeById(id);
    }
}
