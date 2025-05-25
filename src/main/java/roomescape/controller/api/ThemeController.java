package roomescape.controller.api;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponseDto> getAllThemes() {
        return themeService.findAllThemes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeResponseDto postTheme(
            @RequestBody final ThemeCreateRequestDto requestDto
    ) {
        return themeService.createTheme(requestDto);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponseDto> getPopularThemes() {
        return themeService.findPopularThemes();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(@PathVariable("id") final Long id) {
        themeService.deleteThemeById(id);
    }
}
