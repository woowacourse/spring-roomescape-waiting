package roomescape.presentation.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthRequired;
import roomescape.auth.Role;
import roomescape.business.dto.ThemeDto;
import roomescape.business.model.vo.UserRole;
import roomescape.business.service.ThemeService;
import roomescape.presentation.dto.request.ThemeCreateRequest;
import roomescape.presentation.dto.response.ThemeResponse;

@RestController
@RequiredArgsConstructor
public class ThemeApiController {

    private final ThemeService themeService;

    @PostMapping("/themes")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<ThemeResponse> add(@RequestBody @Valid ThemeCreateRequest request) {
        ThemeDto themeDto = themeService.addAndGet(request.name(), request.description(), request.thumbnail());
        return ResponseEntity.created(URI.create("/themes")).body(ThemeResponse.from(themeDto));
    }

    @GetMapping("/themes")
    @AuthRequired
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeDto> themeDtos = themeService.getAll();
        List<ThemeResponse> responses = ThemeResponse.from(themeDtos);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/themes/popular")
    @AuthRequired
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam(value = "size", defaultValue = "10") int size) {
        List<ThemeDto> popularThemeDtos = themeService.getPopular(size);
        List<ThemeResponse> responses = ThemeResponse.from(popularThemeDtos);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/themes/{id}")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
