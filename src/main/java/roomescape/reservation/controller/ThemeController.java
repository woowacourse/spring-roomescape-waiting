package roomescape.reservation.controller;

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
import roomescape.reservation.model.Theme;
import roomescape.reservation.dto.request.ThemeCreateRequest;
import roomescape.reservation.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    private ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Theme createTheme(@RequestBody @Valid ThemeCreateRequest requestBody) {
        return themeService.createTheme(requestBody);
    }

    @GetMapping
    public List<Theme> readAllThemes() {
        return themeService.findAllThemes();
    }

    @GetMapping("/popular")
    public List<Theme> readMostReservedThemes() {
        return themeService.findMostReservedThemes();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteThemeById(@PathVariable("id") Long id) {
        themeService.deleteThemeById(id);
    }
}
