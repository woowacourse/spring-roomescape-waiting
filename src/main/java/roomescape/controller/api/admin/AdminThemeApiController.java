package roomescape.controller.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Theme;
import roomescape.service.dto.request.ThemeSaveRequest;
import roomescape.service.dto.response.ThemeResponse;
import roomescape.service.theme.ThemeCreateService;
import roomescape.service.theme.ThemeDeleteService;

import java.net.URI;

@RestController
public class AdminThemeApiController {

    private final ThemeCreateService themeCreateService;
    private final ThemeDeleteService themeDeleteService;

    public AdminThemeApiController(ThemeCreateService themeCreateService,
                                   ThemeDeleteService themeDeleteService) {
        this.themeCreateService = themeCreateService;
        this.themeDeleteService = themeDeleteService;
    }

    @PostMapping("/api/admin/themes")
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody @Valid
                                                  ThemeSaveRequest request) {
        Theme theme = themeCreateService.createTheme(request);
        return ResponseEntity.created(URI.create("/api/themes/" + theme.getId()))
                .body(new ThemeResponse(theme));
    }

    @DeleteMapping("/api/admin/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable
                                            @Positive(message = "1 이상의 값만 입력해주세요.")
                                            long id) {
        themeDeleteService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
