package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.reservation.controller.dto.request.ThemeSaveRequest;
import roomescape.reservation.controller.dto.response.ThemeDeleteResponse;
import roomescape.reservation.controller.dto.response.ThemeResponse;
import roomescape.reservation.service.ThemeService;

@Controller
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> save(@RequestBody ThemeSaveRequest themeSaveRequest) {
        ThemeResponse themeResponse = themeService.save(themeSaveRequest);
        return ResponseEntity.created(URI.create("/themes/" + themeResponse.themeId()))
                .body(themeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAll() {
        return ResponseEntity.ok(themeService.getAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ThemeResponse>> findThemeRanking() {
        return ResponseEntity.ok(themeService.findThemeRanking());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ThemeDeleteResponse> delete(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(themeService.delete(id));
    }
}
