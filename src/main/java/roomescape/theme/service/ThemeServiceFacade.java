package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@Service
public class ThemeServiceFacade {
    private final ThemeService themeService;

    public ThemeServiceFacade(
        ThemeService themeService
    ) {
        this.themeService = themeService;
    }

    public ThemeResponse createTheme(ThemeCreateRequest request) {
        return themeService.createTheme(request);
    }

    public List<ThemeResponse> findAll() {
        return themeService.findAll();
    }

    public void deleteThemeById(Long id) {
        themeService.deleteThemeById(id);
    }

    public List<ThemeResponse> findLimitedThemesByPopularDesc() {
        return themeService.findLimitedThemesByPopularDesc();
    }
}
