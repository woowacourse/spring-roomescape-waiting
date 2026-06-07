package roomescape.theme.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.controller.dto.ThemeCreateRequest;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.controller.dto.ThemeUpdateRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/admin/themes")
public class ThemeAdminController {
    private final ThemeService themeService;

    public ThemeAdminController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(
            @Valid @RequestBody ThemeCreateRequest request) {
        Theme theme = themeService.create(request.name(), request.description(), request.thumbnail());

        return ResponseEntity.status(CREATED)
                .body(ThemeResponse.from(theme));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @Positive(message = "테마 id는 1 이상의 숫자여야 합니다.") @PathVariable Long id,
            @Valid @RequestBody ThemeUpdateRequest request) {
        if (request.status().isActive()) {
            themeService.activate(id);
            return ResponseEntity.noContent().build();
        }

        themeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
