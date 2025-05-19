package roomescape.theme.application;

import roomescape.theme.ui.dto.CreateThemeWebRequest;
import roomescape.theme.ui.dto.ThemeResponse;

import java.util.List;

public interface ThemeFacade {

    List<ThemeResponse> getAll();

    List<ThemeResponse> getRanking();

    ThemeResponse create(CreateThemeWebRequest createThemeWebRequest);

    void delete(Long id);
}
