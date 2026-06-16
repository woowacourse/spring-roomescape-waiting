package roomescape.theme.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.theme.controller.dto.request.ThemeActiveUpdateDto;
import roomescape.theme.controller.dto.request.ThemeSaveDto;
import roomescape.theme.controller.dto.response.PopularThemeDetailDto;
import roomescape.theme.controller.dto.response.ThemeDetailDto;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

import java.util.List;

import static roomescape.member.domain.Role.MANAGER;

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
        Theme theme = themeService.register(dto.name(), dto.description(), dto.thumbnailUrl(), dto.amount());
        ThemeDetailDto responseData = ThemeDetailDto.from(theme);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/themes/{id}")
    public ResponseEntity<ThemeDetailDto> updateThemeStatus(@PathVariable Long id, @Validated @RequestBody ThemeActiveUpdateDto dto) {
        Theme theme = themeService.updateStatus(id, dto.isActive());
        ThemeDetailDto responseData = ThemeDetailDto.from(theme);
        return ResponseEntity.ok(responseData);
    }

}
