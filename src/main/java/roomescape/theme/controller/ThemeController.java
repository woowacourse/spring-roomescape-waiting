package roomescape.theme.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.system.auth.annotation.Admin;
import roomescape.system.dto.response.ApiResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.dto.ThemesResponse;
import roomescape.theme.service.ThemeService;

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
        return ApiResponse.success(themeService.getTop10Themes(today));
    }

    @Admin
    @PostMapping("/themes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ThemeResponse> saveTheme(
            @Valid @RequestBody final ThemeRequest request,
            final HttpServletResponse response
    ) {
        final ThemeResponse themeResponse = themeService.addTheme(request);
        response.setHeader(HttpHeaders.LOCATION, "/themes/" + themeResponse.id());

        return ApiResponse.success(themeResponse);
    }

    @Admin
    @DeleteMapping("/themes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeTheme(
            @NotNull(message = "themeId는 null일 수 없습니다.") @PathVariable final Long id
    ) {
        themeService.removeThemeById(id);

        return ApiResponse.success();
    }
}
