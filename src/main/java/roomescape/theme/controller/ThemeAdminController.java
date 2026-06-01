package roomescape.theme.controller;

import static roomescape.member.domain.Role.MANAGER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.theme.controller.dto.request.ThemeActiveUpdateDto;
import roomescape.theme.controller.dto.request.ThemeSaveDto;
import roomescape.theme.controller.dto.response.PopularThemeDetailDto;
import roomescape.theme.controller.dto.response.ThemeDetailDto;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ThemeAdminController {

    private final ThemeService themeService;

    @AuthGuard(roles = MANAGER)
    @GetMapping("/themes")
    public ResponseEntity<List<ThemeDetailDto>> getThemes() {
        List<ThemeDetailDto> responseData = themeService.readThemes().stream()
            .map(ThemeDetailDto::from)
            .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @GetMapping("/themes/popular")
    public ResponseEntity<List<PopularThemeDetailDto>> getPopularThemes(@RequestParam int top) {
        List<PopularThemeDetailDto> responseData = themeService.readPopularThemes(top).stream()
            .map(PopularThemeDetailDto::from)
            .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PostMapping("/themes")
    public ResponseEntity<ThemeDetailDto> createTheme(@Validated @RequestBody ThemeSaveDto dto) {
        Theme theme = themeService.register(dto.name(), dto.description(), dto.thumbnailUrl());
        ThemeDetailDto responseData = ThemeDetailDto.from(theme);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/themes/{id}")
    public ResponseEntity<ThemeDetailDto> updateThemeStatus(@PathVariable Long id,
        @Validated @RequestBody ThemeActiveUpdateDto dto) {
        Theme theme = themeService.updateStatus(id, dto.isActive());
        ThemeDetailDto responseData = ThemeDetailDto.from(theme);
        return ResponseEntity.ok(responseData);
    }

}
