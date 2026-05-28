package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.domain.theme.Theme;
import roomescape.service.ThemeService;

@RestController
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/admin/themes")
    @ResponseStatus(HttpStatus.CREATED)
    public ThemeResponse create(@Valid @RequestBody ThemeCreateRequest request) {
        Theme theme = themeService.create(request);
        return ThemeResponse.toDto(theme);
    }

    @GetMapping("/themes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ThemeResponse find(@PathVariable Long id) {
        Theme theme = themeService.find(id);
        return ThemeResponse.toDto(theme);
    }

    @GetMapping("/themes/famous")
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponse> findFamous(@Valid @ModelAttribute ThemeFamousFindRequest request) {
        List<Theme> themes = themeService.findFamous(request, LocalDate.now());
        return themes.stream()
                .map(ThemeResponse::toDto)
                .toList();
    }

    @GetMapping("/themes")
    @ResponseStatus(HttpStatus.OK)
    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeService.findAll();
        return themes.stream()
                .map(ThemeResponse::toDto)
                .toList();
    }

    @DeleteMapping({"/admin/themes/{id}"})
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable long id) {
        themeService.delete(id);
    }
}
