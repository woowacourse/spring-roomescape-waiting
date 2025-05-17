package roomescape.theme.controller;

import static roomescape.theme.controller.response.ThemeSuccessCode.CREATE_THEME;
import static roomescape.theme.controller.response.ThemeSuccessCode.DELETE_THEME;
import static roomescape.theme.controller.response.ThemeSuccessCode.GET_POPULAR_THEMES;
import static roomescape.theme.controller.response.ThemeSuccessCode.GET_THEMES;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeApiController {

    private final ThemeService themeService;

    public ThemeApiController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getThemes() {
        List<ThemeResponse> responses = themeService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(GET_THEMES, responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ThemeResponse>> createTheme(@RequestBody @Valid ThemeCreateRequest request) {
        ThemeResponse response = themeService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(CREATE_THEME, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTheme(@PathVariable Long id) {
        themeService.deleteById(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(DELETE_THEME));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> popularThemes() {
        List<ThemeResponse> popularThemes = themeService.getPopularThemes();

        return ResponseEntity.ok(
                ApiResponse.success(GET_POPULAR_THEMES, popularThemes));
    }
}
