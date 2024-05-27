package roomescape.controller.reservation;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ThemeService;
import roomescape.service.dto.theme.PopularThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@RestController
public class ThemeRestController {

    private final ThemeService themeService;

    public ThemeRestController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/themes")
    public List<ThemeResponse> findAllThemes() {
        return themeService.findAllThemes();
    }

    @GetMapping("/themes/popular")
    public List<ThemeResponse> findTopBookedThemes(@RequestParam(name = "start-date") String startDate,
                                                   @RequestParam(name = "end-date") String endDate,
                                                   @RequestParam Integer count) {
        return themeService.findTopBookedThemes(new PopularThemeRequest(startDate, endDate, count));
    }
}
