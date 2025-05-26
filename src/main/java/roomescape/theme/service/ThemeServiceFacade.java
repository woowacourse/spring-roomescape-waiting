package roomescape.theme.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

import java.util.List;

@Service
public class ThemeServiceFacade {
    private final ThemeService themeService;

    public ThemeServiceFacade(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Transactional
    public ThemeResponse createTheme(ThemeCreateRequest request) {
        return themeService.createTheme(request);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeService.findAll();
    }

    @Transactional
    public void deleteThemeById(Long id) {
        themeService.deleteThemeById(id);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findLimitedThemesByPopularDesc() {
        return themeService.findLimitedThemesByPopularDesc();
    }
}
