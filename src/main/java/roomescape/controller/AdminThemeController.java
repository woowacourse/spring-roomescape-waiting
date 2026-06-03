package roomescape.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.controller.dto.ThemeRequest;
import roomescape.global.AdminOnly;
import roomescape.service.ThemeService;

@AdminOnly
@RequestMapping("/admin/themes")
@RestController
@Validated
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ThemeRequest themeRequest) {
        Long themeId = themeService.saveTheme(themeRequest);
        return ResponseEntity
                .created(URI.create("/themes/" + themeId))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
