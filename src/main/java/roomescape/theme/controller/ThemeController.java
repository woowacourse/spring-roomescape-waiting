package roomescape.theme.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.dto.response.ApiResponse;
import roomescape.theme.dto.ThemesResponse;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;

@RestController
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ThemesResponse> getAllThemes() {
        return ApiResponse.success(themeService.findAllThemes());
    }

    @GetMapping("/themes/top")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ThemesResponse> getTop10Themes(
            @NotNull(message = "날짜는 null일 수 없습니다.") final LocalDate today
    ) {
        return ApiResponse.success(themeService.findTop10Themes(today));
    }
}
