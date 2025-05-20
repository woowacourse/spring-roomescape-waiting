package roomescape.controller.api;

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
import roomescape.controller.dto.request.ThemeRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    private ThemeController(ThemeService themeService) {
        this.themeService = themeService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeResponse createTheme(@RequestBody @Valid ThemeRequest request) {
        return ThemeResponse.from(themeService.addTheme(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(@PathVariable("id") long id) {
        themeService.removeTheme(id);
    }
}
