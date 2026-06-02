package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Theme;
import roomescape.dto.theme.command.CreateThemeCommand;
import roomescape.dto.theme.request.CreateThemeRequest;
import roomescape.dto.theme.response.ThemeResponse;
import roomescape.infrastructure.AdminOnly;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/admin/themes")
@AdminOnly
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody CreateThemeRequest createThemeRequest) {
        Theme createdTheme = themeService.createTheme(CreateThemeCommand.from(createThemeRequest));
        URI location = URI.create("/themes/" + createdTheme.getId());
        return ResponseEntity.created(location).body(ThemeResponse.from(createdTheme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.ok().build();
    }
}
