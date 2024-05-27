package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ThemeCreateRequest;
import roomescape.service.dto.response.ThemeResponse;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createRoomTheme(
            @RequestBody @Valid ThemeCreateRequest themeCreateRequest) {
        ThemeResponse themeResponse = themeService.save(themeCreateRequest);
        return ResponseEntity.created(URI.create("/themes" + themeResponse.id()))
                .body(themeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomTheme(@PathVariable Long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
