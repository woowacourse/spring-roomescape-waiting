package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ThemeService;
import roomescape.dto.request.ThemeRegisterDto;
import roomescape.dto.response.ThemeResponseDto;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponseDto> getThemes() {
        return themeService.getAllThemes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeResponseDto addTheme(@RequestBody @Valid ThemeRegisterDto themeRegisterDto) {
        return themeService.saveTheme(themeRegisterDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(@PathVariable("id") Long id) {
        themeService.deleteTheme(id);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponseDto> getPopularThemes(@RequestParam String date) {
        return themeService.findPopularThemes(date);
    }
}
