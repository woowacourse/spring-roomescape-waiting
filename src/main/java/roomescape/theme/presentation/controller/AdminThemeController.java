package roomescape.theme.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.presentation.dto.ThemeCreateRequest;
import roomescape.theme.presentation.dto.ThemeResponse;

@RequiredArgsConstructor
@RequestMapping("/admin/themes")
@RestController
public class AdminThemeController {

    private final ThemeService themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> create(
            @Valid @RequestBody ThemeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(themeService.save(request.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        int deletedCount = themeService.delete(id);

        if (deletedCount == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
