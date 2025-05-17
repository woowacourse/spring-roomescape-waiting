package roomescape.theme.application.service;

import roomescape.theme.application.dto.CreateThemeServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;

public interface ThemeCommandService {

    Theme create(CreateThemeServiceRequest createThemeServiceRequest);

    void delete(ThemeId id);
}
