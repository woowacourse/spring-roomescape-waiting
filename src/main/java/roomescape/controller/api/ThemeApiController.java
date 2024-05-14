package roomescape.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Theme;
import roomescape.service.dto.request.ThemeSaveRequest;
import roomescape.service.dto.response.ThemeResponse;
import roomescape.service.theme.ThemeCreateService;
import roomescape.service.theme.ThemeDeleteService;
import roomescape.service.theme.ThemeFindService;

import java.net.URI;
import java.util.List;

@Validated
@RestController
public class ThemeApiController {

    private final ThemeCreateService themeCreateService;
    private final ThemeFindService themeFindService;
    private final ThemeDeleteService themeDeleteService;

    public ThemeApiController(ThemeCreateService themeCreateService,
                              ThemeFindService themeFindService,
                              ThemeDeleteService themeDeleteService) {
        this.themeCreateService = themeCreateService;
        this.themeFindService = themeFindService;
        this.themeDeleteService = themeDeleteService;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<Theme> themes = themeFindService.findThemes();
        return ResponseEntity.ok(
                themes.stream()
                        .map(ThemeResponse::new)
                        .toList()
        );
    }

    @GetMapping("/themes/ranks")
    public ResponseEntity<List<ThemeResponse>> getThemeRanks() {
        List<Theme> themes = themeFindService.findThemeRanks();
        return ResponseEntity.ok(
                themes.stream()
                        .map(ThemeResponse::new)
                        .toList()
        );
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> addTheme(@RequestBody @Valid ThemeSaveRequest request) {
        Theme theme = themeCreateService.createTheme(request);
        return ResponseEntity.created(URI.create("/themes/" + theme.getId()))
                .body(new ThemeResponse(theme));
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable
                                            @Positive(message = "1 이상의 값만 입력해주세요.") long id) {
        themeDeleteService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
