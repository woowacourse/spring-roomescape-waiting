package roomescape.theme.controller.api;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.controller.dto.request.ThemeRequest;
import roomescape.theme.controller.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    private ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeResponse createTheme(@RequestBody @Valid ThemeRequest request) {
        return ThemeResponse.from(themeService.addTheme(request));
    }

    @GetMapping
    public List<ThemeResponse> readThemes() {
        return themeService.findAllThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping("/top-rank")
    public List<ThemeResponse> readTopRankTheme() {
        return themeService.findTopReservedThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(final @PathVariable("id") long id) {
        themeService.removeTheme(id);
    }
}
