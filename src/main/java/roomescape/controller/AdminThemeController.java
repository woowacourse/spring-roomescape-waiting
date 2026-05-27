package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ControllerThemeCreateRequest;
import roomescape.controller.dto.response.ControllerThemeResponse;
import roomescape.service.ThemeService;
import roomescape.service.dto.response.ServiceThemeResponse;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ControllerThemeResponse> save(@RequestBody ControllerThemeCreateRequest requestDto) {
        ServiceThemeResponse serviceResponse = themeService.save(requestDto.toServiceThemeRequest());
        ControllerThemeResponse controllerResponse = ControllerThemeResponse.from(serviceResponse);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(controllerResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
