package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.command.CreateThemeService;
import roomescape.application.reservation.command.DeleteThemeService;
import roomescape.presentation.api.reservation.request.CreateThemeRequest;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private static final String THEMES_URL = "/themes/%d";

    private final CreateThemeService createThemeService;
    private final DeleteThemeService deleteThemeService;

    public AdminThemeController(CreateThemeService createThemeService,
                                DeleteThemeService deleteThemeService) {
        this.createThemeService = createThemeService;
        this.deleteThemeService = deleteThemeService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateThemeRequest createThemeRequest) {
        Long id = createThemeService.register(createThemeRequest.toCreateCommand());
        return ResponseEntity.created(URI.create(THEMES_URL.formatted(id)))
                .build();
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> delete(@PathVariable Long themeId) {
        deleteThemeService.removeById(themeId);
        return ResponseEntity.noContent().build();
    }
}
