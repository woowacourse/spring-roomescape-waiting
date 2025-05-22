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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ThemeService;
import roomescape.domain.theme.Theme;
import roomescape.presentation.request.CreateThemeRequest;
import roomescape.presentation.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService service;

    public ThemeController(final ThemeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public ThemeResponse register(@RequestBody @Valid final CreateThemeRequest request) {
        Theme theme = service.register(request.name(), request.description(), request.thumbnail());

        return ThemeResponse.from(theme);
    }

    @GetMapping
    public List<ThemeResponse> getAllThemes() {
        List<Theme> themes = service.findAllThemes();

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping(value = "/popular", params = {"startDate", "endDate", "count"})
    public List<ThemeResponse> getAvailableTimes(
            @RequestParam("startDate") final LocalDate startDate,
            @RequestParam("endDate") final LocalDate endDate,
            @RequestParam("count") final Integer count) {

        List<Theme> themes = service.findPopularThemes(startDate, endDate, count);

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable("id") final long id) {
        service.removeById(id);
    }
}
