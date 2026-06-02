package roomescape.theme.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.member.domain.Role;
import roomescape.theme.controller.dto.response.ThemeDetailDto;
import roomescape.theme.service.ThemeService;

import java.util.List;

import static roomescape.member.domain.Role.MANAGER;
import static roomescape.member.domain.Role.MEMBER;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @AuthGuard(roles = {MEMBER, MANAGER})
    @GetMapping("/themes")
    public ResponseEntity<List<ThemeDetailDto>> getActiveThemes() {
        List<ThemeDetailDto> responseData = themeService.readSlotOfThemes().stream()
                .map(ThemeDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/themes/popular")
    public ResponseEntity<List<ThemeDetailDto>> getPopularThemes(@RequestParam int top) {
        List<ThemeDetailDto> responseData = themeService.readPopularThemes(top).stream()
                .map(ThemeDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

}
