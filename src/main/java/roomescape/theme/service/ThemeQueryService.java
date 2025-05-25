package roomescape.theme.service;

import java.util.List;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;

public interface ThemeQueryService {
    List<ThemeResponse> getAll();

    Theme getTheme(Long id);

    List<ThemeResponse> getPopularThemes();

}
