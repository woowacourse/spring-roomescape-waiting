package roomescape.controller.admin.api;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ThemeService;
import roomescape.application.service.result.ThemeRegisterResult;
import roomescape.controller.admin.api.dto.request.AdminThemeRequest;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;
import roomescape.controller.admin.api.query.AdminThemeQuery;

@RequestMapping("/api/admin/themes")
@RestController
@RequiredArgsConstructor
@Validated
public class AdminThemeApiController {

    private final ThemeService themeService;
    private final AdminThemeQuery themeQuery;

    @PostMapping
    public ResponseEntity<AdminThemeResponse> register(@Valid @RequestBody AdminThemeRequest request) {
        ThemeRegisterResult result = themeService.register(request.toCommand());
        return ResponseEntity.status(CREATED).body(AdminThemeResponse.from(result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable
            @Positive(message = "테마 비활성화 식별자는 양수여야 합니다.") Long id
    ) {
        themeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable
            @Positive(message = "테마 활성화 식별자는 양수여야 합니다.") Long id
    ) {
        themeService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AdminThemeResponse>> getAllThemes() {
        return ResponseEntity.ok(themeQuery.getAllThemes());
    }
}
