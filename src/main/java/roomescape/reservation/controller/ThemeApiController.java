package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.dto.ResourcesResponse;
import roomescape.reservation.dto.PopularThemeResponse;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.dto.ThemeSaveRequest;
import roomescape.reservation.service.ThemeService;

@RestController
public class ThemeApiController {

    private final ThemeService themeService;

    public ThemeApiController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes/popular")
    public ResponseEntity<ResourcesResponse<PopularThemeResponse>> findTopTenThemesOfLastWeek(
            @RequestParam(value = "limitCount", defaultValue = "10") int limitCount
    ) {
        List<PopularThemeResponse> popularThemes = themeService.findThemesDescOfLastWeekForLimitCount(limitCount);
        ResourcesResponse<PopularThemeResponse> response = new ResourcesResponse<>(popularThemes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/themes")
    public ResponseEntity<ResourcesResponse<ThemeResponse>> findAll() {
        List<ThemeResponse> themes = themeService.findAll();
        ResourcesResponse<ThemeResponse> response = new ResourcesResponse<>(themes);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> save(@Valid @RequestBody ThemeSaveRequest themeSaveRequest) {
        ThemeResponse themeResponse = themeService.save(themeSaveRequest);

        return ResponseEntity.created(URI.create("/themes/" + themeResponse.id())).body(themeResponse);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        themeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
