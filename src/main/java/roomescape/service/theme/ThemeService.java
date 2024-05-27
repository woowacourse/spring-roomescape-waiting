package roomescape.service.theme;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.theme.module.ThemeDeleteService;
import roomescape.service.theme.module.ThemeRegisterService;
import roomescape.service.theme.module.ThemeSearchService;

@Service
public class ThemeService {

    private final ThemeRegisterService themeRegisterService;
    private final ThemeSearchService themeSearchService;
    private final ThemeDeleteService themeDeleteService;

    public ThemeService(ThemeRegisterService themeRegisterService,
                        ThemeSearchService themeSearchService,
                        ThemeDeleteService themeDeleteService
    ) {
        this.themeRegisterService = themeRegisterService;
        this.themeSearchService = themeSearchService;
        this.themeDeleteService = themeDeleteService;
    }

    public Long addTheme(ThemeRequest themeRequest) {
        return themeRegisterService.resisterTheme(themeRequest);
    }

    public ThemeResponse findTheme(Long themeId) {
        return themeSearchService.findTheme(themeId);
    }

    public List<ThemeResponse> getAllTheme() {
        return themeSearchService.findAllThemes();
    }

    public List<ThemeResponse> getPopularThemes() {
        return themeSearchService.findPopularThemes();
    }

    public void deleteTheme(Long themeId) {
        themeDeleteService.deleteTheme(themeId);
    }
}
