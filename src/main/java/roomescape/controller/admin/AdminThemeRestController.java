package roomescape.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@RestController
public class AdminThemeRestController {

    private final ThemeService themeService;

    public AdminThemeRestController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/themes")
    public ThemeResponse createTheme(@Valid @RequestBody ThemeRequest request) {
        return themeService.createTheme(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/themes/{id}")
    public void deleteTheme(@PathVariable long id) {
        themeService.deleteTheme(id);
    }
}
