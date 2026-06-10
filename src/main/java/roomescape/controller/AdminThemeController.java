package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.facade.ThemeFacade;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final ThemeFacade themeFacade;

    public AdminThemeController(ThemeFacade themeFacade) {
        this.themeFacade = themeFacade;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> save(@RequestBody ThemeCreateRequest request) {
        ThemeResponse response = themeFacade.save(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeFacade.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
