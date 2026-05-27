package roomescape.theme.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.controller.dto.response.ThemeDetailDto;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeDetailDto>> getActiveThemes() {
        List<ThemeDetailDto> responseData = themeService.readActiveThemes().stream()
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
