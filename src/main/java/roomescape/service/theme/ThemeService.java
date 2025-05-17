package roomescape.service.theme;

import java.util.List;
import roomescape.dto.theme.PopularThemeResponse;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;

public interface ThemeService {
    ThemeResponse create(ThemeRequest request);

    List<ThemeResponse> getAll();

    void deleteById(Long id);

    List<PopularThemeResponse> getPopularThemes();
}
