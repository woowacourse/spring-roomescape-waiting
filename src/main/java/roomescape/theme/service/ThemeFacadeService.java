package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.theme.presentation.dto.ThemeResponse;

@Service
public class ThemeFacadeService {

    private final ThemeQueryService themeQueryService;
    private final ThemeCommandService themeCommandService;

    public ThemeFacadeService(final ThemeQueryService themeQueryService, final ThemeCommandService themeCommandService) {
        this.themeQueryService = themeQueryService;
        this.themeCommandService = themeCommandService;
    }

    public ThemeResponse createTheme(final ThemeRequest request) {
        Theme theme = themeCommandService.save(request);

        return ThemeResponse.from(theme);
    }

    public void deleteThemeById(final Long id) {
        themeCommandService.deleteById(id);
    }

    public List<ThemeResponse> getThemes() {
        return themeQueryService.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }

    public List<PopularThemeResponse> getPopularThemes() {
        return themeQueryService.getPopularThemes().stream()
            .map(theme -> new PopularThemeResponse(theme.getName(), theme.getDescription(), theme.getThumbnail()))
            .toList();
    }
}
