package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ThemeRequestDTO;
import roomescape.dto.ThemeResponseDTO;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping("/themes")
    public ResponseEntity<Void> add(
            @Valid @RequestBody ThemeRequestDTO request) {
        ThemeResponseDTO saved = themeService.addTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + saved.id())).build();
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @Positive(message = "테마 ID는 1 이상의 숫자여야 합니다.") Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
