package roomescape.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.dto.ThemeRequest;
import roomescape.service.AdminThemeService;

@RequestMapping("/admin/themes")
@RestController
@Validated
public class AdminThemeController {
    private final AdminThemeService adminThemeService;

    public AdminThemeController(AdminThemeService adminThemeService) {
        this.adminThemeService = adminThemeService;
    }

    @PostMapping
    public ResponseEntity<Void> createTheme(@Valid @RequestBody ThemeRequest themeRequest) {
        Long themeId = adminThemeService.save(themeRequest);
        URI location = URI.create("/themes/" + themeId);
        return ResponseEntity.created(location).build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteTheme(@PathVariable long id) {
        adminThemeService.delete(id);
    }
}
