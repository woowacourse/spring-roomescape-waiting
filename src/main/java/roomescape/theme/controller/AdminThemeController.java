package roomescape.theme.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;
import roomescape.theme.service.AdminThemeService;

@Tag(name = "어드민 테마", description = "테마 생성·조회·삭제 API (관리자용)")
@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final AdminThemeService adminThemeService;

    public AdminThemeController(AdminThemeService adminThemeService) {
        this.adminThemeService = adminThemeService;
    }

    @PostMapping
    public ResponseEntity<AdminThemeResponse> createTheme(@Valid @RequestBody AdminThemeRequest request) {
        AdminThemeResponse response = adminThemeService.createTheme(request);
        return ResponseEntity.created(URI.create("/admin/themes/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AdminThemeResponse>> getThemes() {
        return ResponseEntity.ok(adminThemeService.getAllThemes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        adminThemeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
