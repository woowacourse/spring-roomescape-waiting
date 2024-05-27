package roomescape.controller.theme;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservation.dto.PopularThemeResponse;
import roomescape.controller.theme.dto.PopularThemeRequest;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public List<ThemeResponse> getThemes() {
        return themeService.getThemes();
    }

    @GetMapping(value = "/popular", params = {"from", "until", "limit"})
    public List<PopularThemeResponse> getPopularThemes(
            @Valid final PopularThemeRequest popularThemeRequest) {
        return themeService.findMostBookedThemes(popularThemeRequest.from(),
                popularThemeRequest.until(),
                popularThemeRequest.limit());
    }
}
