package roomescape.controller.api;

import java.util.List;
import org.springframework.http.HttpStatus;
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
import roomescape.service.query.ThemeQueryService;
import roomescape.service.command.ThemeCommandService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeQueryService themeQueryService;
    private final ThemeCommandService themeCommandService;

    public ThemeController(ThemeQueryService themeQueryService, ThemeCommandService themeCommandService) {
        this.themeQueryService = themeQueryService;
        this.themeCommandService = themeCommandService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponseDto> getAllThemes() {
        return themeQueryService.findAllThemes();
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponseDto> getPopularThemes() {
        return themeQueryService.findPopularThemes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeResponseDto postTheme(
            @RequestBody final ThemeCreateRequestDto requestDto
    ) {
        return themeCommandService.createTheme(requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(@PathVariable("id") final Long id) {
        themeCommandService.deleteThemeById(id);
    }
}
