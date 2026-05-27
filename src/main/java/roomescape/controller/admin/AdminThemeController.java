package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.ThemeRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.dto.command.ThemeCommand;
import roomescape.service.dto.result.ThemeResult;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {
    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ThemeResponse> createTheme(@Valid @ModelAttribute ThemeRequest request) {
        ThemeResult result = themeService.create(ThemeCommand.from(request));
        ThemeResponse response = ThemeResponse.from(result);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}