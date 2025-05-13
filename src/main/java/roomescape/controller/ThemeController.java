package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.UnauthorizedAccessException;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> allThemes = themeService.findAllThemes();
        return ResponseEntity.ok(allThemes);
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> addTheme(@Valid @RequestBody final ThemeCreateRequest requestDto, Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        ThemeResponse responseDto = themeService.createTheme(requestDto);
        return ResponseEntity.created(URI.create("themes/" + responseDto.id())).body(responseDto);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes() {
        List<ThemeResponse> popularThemes = themeService.findPopularThemes();
        return ResponseEntity.ok(popularThemes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") final Long id, Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }
}
