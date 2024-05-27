package roomescape.service.theme;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.theme.module.ThemeDeleteService;
import roomescape.service.theme.module.ThemeResisterService;
import roomescape.service.theme.module.ThemeSearchService;

@Service
public class ThemeService {

    private final ThemeResisterService themeResisterService;
    private final ThemeSearchService themeSearchService;
    private final ThemeDeleteService themeDeleteService;

    public ThemeService(ThemeResisterService themeResisterService,
                        ThemeSearchService themeSearchService,
                        ThemeDeleteService themeDeleteService
    ) {
        this.themeResisterService = themeResisterService;
        this.themeSearchService = themeSearchService;
        this.themeDeleteService = themeDeleteService;
    }

    public Long addTheme(ThemeRequest themeRequest) {
        return themeResisterService.resisterTheme(themeRequest);
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
