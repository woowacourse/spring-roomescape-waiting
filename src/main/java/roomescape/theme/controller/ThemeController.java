package roomescape.theme.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.member.entity.RoleType;
import roomescape.theme.dto.request.ThemeRequest.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse.ThemeCreateResponse;
import roomescape.theme.dto.response.ThemeResponse.ThemeReadResponse;
import roomescape.theme.service.ThemeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    @PostMapping
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<ThemeCreateResponse> createTheme(
            @RequestBody @Valid ThemeCreateRequest request
    ) {
        ThemeCreateResponse response = themeService.createTheme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeReadResponse>> getAllThemes() {
        List<ThemeReadResponse> responses = themeService.getAllThemes();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeReadResponse>> getPopularThemes(
            @RequestParam("limit") int limit
    ) {
        List<ThemeReadResponse> response = themeService.getPopularThemes(limit);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> deleteTheme(
            @PathVariable("id") Long id
    ) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
